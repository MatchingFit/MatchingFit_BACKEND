package com.example.matching_fit.domain.score.service;

import com.example.matching_fit.domain.manager.manager_competency_score.entity.ManagerCompetencyScore;
import com.example.matching_fit.domain.manager.manager_competency_score.repository.ManagerCompetencyScoreRepository;
import com.example.matching_fit.domain.resume.entity.Resume;
import com.example.matching_fit.domain.resume.repository.ResumeRepository;
import com.example.matching_fit.domain.score.dto.CompetencyScoreTop3Dto;
import com.example.matching_fit.domain.score.dto.ResumeSimilarityDto;
import com.example.matching_fit.domain.score.repository.CompetencyScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompetencyScoreService {

    private final CompetencyScoreRepository competencyScoreRepository;
    private final ManagerCompetencyScoreRepository scoreRepository;
    private final ResumeRepository resumeRepository;

    public List<CompetencyScoreTop3Dto> getTop3CompetenciesPerResume() {
        List<Object[]> result = competencyScoreRepository.findAllWithRanking();

        return result.stream()
                .filter(row -> ((Number) row[3]).intValue() <= 3) // rank <= 3
                .map(row -> new CompetencyScoreTop3Dto(
                        ((Number) row[0]).longValue(), // resumeId
                        ((Number) row[1]).longValue(), // competencyId
                        ((Number) row[2]).doubleValue(), // totalScore
                        ((Number) row[3]).intValue() // rank
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResumeSimilarityDto> getTop5SimilarResumesWithInfo(Long managerId) {
        // 1. 매니저 Top3 역량(competencyId, rank) 조회
        List<Object[]> managerTop3Raw = scoreRepository.findTop3CompetencyScoresWithRank(managerId);
        Map<Long, Integer> managerTop3Map = managerTop3Raw.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[2]).intValue()
                ));

        // 2. 이력서 Top3 역량 (resumeId, competencyId, rank) 조회
        List<Object[]> resumeTop3Raw = competencyScoreRepository.findAllWithRanking();
        List<CompetencyScoreTop3Dto> resumeTop3List = resumeTop3Raw.stream()
                .filter(row -> ((Number) row[3]).intValue() <= 3)
                .map(row -> new CompetencyScoreTop3Dto(
                        ((Number) row[0]).longValue(),
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).doubleValue(),
                        ((Number) row[3]).intValue()
                ))
                .toList();

        // 3. 이력서별 역량 맵 구성
        Map<Long, Map<Long, Integer>> resumeToCompetencyRankMap = resumeTop3List.stream()
                .collect(Collectors.groupingBy(
                        CompetencyScoreTop3Dto::getResumeId,
                        Collectors.toMap(
                                CompetencyScoreTop3Dto::getCompetencyId,
                                CompetencyScoreTop3Dto::getRank
                        )
                ));

        // 4. 유사도 계산
        Map<Long, Integer> resumeSimilarityMap = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Integer>> entry : resumeToCompetencyRankMap.entrySet()) {
            Long resumeId = entry.getKey();
            Map<Long, Integer> competencyRankMap = entry.getValue();

            int similarityScore = 0;
            for (Map.Entry<Long, Integer> managerEntry : managerTop3Map.entrySet()) {
                Long competencyId = managerEntry.getKey();
                Integer managerRank = managerEntry.getValue();

                if (competencyRankMap.containsKey(competencyId)) {
                    int resumeRank = competencyRankMap.get(competencyId);
                    int rankScore = Math.max(0, 4 - Math.abs(managerRank - resumeRank));
                    similarityScore += rankScore;
                }
            }
            if (similarityScore > 0) {
                resumeSimilarityMap.put(resumeId, similarityScore);
            }
        }

        // 5. 유사도 상위 5개 이력서 ID 추출
        List<Long> top5ResumeIds = resumeSimilarityMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        if (top5ResumeIds.isEmpty()) {
            return List.of(); // 유사 이력서 없으면 빈 리스트 반환
        }

        // 6. DTO 리스트 조회 (이미 DTO로 반환됨)
        List<ResumeSimilarityDto> resumeDtos = resumeRepository.findResumeSimilarityDtosByIds(top5ResumeIds);

        // 7. DTO 리스트에 similarityScore 매핑 후 정렬 (similarityScore 매개변수는 쿼리용 자리 채움, 실제 점수는 아래에서 설정)
        Map<Long, Integer> similarityScoreMap = resumeSimilarityMap;
        List<ResumeSimilarityDto> result = resumeDtos.stream()
                .map(dto -> new ResumeSimilarityDto(
                        dto.getResumeId(),
                        dto.getFileUrl(),
                        dto.getJobField(),
                        dto.getUserId(),
                        similarityScoreMap.getOrDefault(dto.getResumeId(), 0)
                ))
                .sorted((a, b) -> Integer.compare(b.getSimilarityScore(), a.getSimilarityScore()))
                .toList();

        return result;
    }


}
