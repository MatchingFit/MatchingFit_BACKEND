package com.example.matching_fit.domain.manager.manager_competency_score.service;
import com.example.matching_fit.domain.manager.manager_competency_score.dto.ManagerCompetencyRankDto;
import com.example.matching_fit.domain.manager.manager_competency_score.dto.ResumeMatchingResultDto;
import com.example.matching_fit.domain.manager.manager_competency_score.entity.ManagerCompetencyScore;
import com.example.matching_fit.domain.manager.manager_competency_score.repository.ManagerCompetencyScoreRepository;
import com.example.matching_fit.domain.manager.resume_matching_result.entity.ResumeMatchingResult;
import com.example.matching_fit.domain.manager.resume_matching_result.repository.ResumeMatchingResultRepository;
import com.example.matching_fit.domain.resume.entity.Resume;
import com.example.matching_fit.domain.resume.repository.ResumeRepository;
import com.example.matching_fit.domain.score.dto.CompetencyScoreTop3Dto;
import com.example.matching_fit.domain.score.dto.ResumeSimilarityDto;
import com.example.matching_fit.domain.score.entity.Competency;
import com.example.matching_fit.domain.score.entity.CompetencyScore;
import com.example.matching_fit.domain.score.repository.CompetencyRepository;
import com.example.matching_fit.domain.score.repository.CompetencyScoreRepository;
import com.example.matching_fit.domain.user.entity.User;
import com.example.matching_fit.domain.user.enums.Role;
import com.example.matching_fit.domain.user.repository.UserRepository;
import com.example.matching_fit.global.security.rq.Rq;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.Manager;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerCompetencyScoreService {
    private final CompetencyRepository competencyRepository;
    private final ManagerCompetencyScoreRepository scoreRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final Rq rq;
    private final ResumeMatchingResultRepository resumeMatchingResultRepository;
    private final CompetencyScoreRepository competencyScoreRepository;

    @Transactional
    public ResumeMatchingResultDto matchResumesByFullRanking(Map<String, Integer> scores) {
        Long managerId = rq.getActor().getId();
        // 1. 인사담당자 점수 저장 또는 업데이트 (필요 시)
        // 1. 점수 저장 - 인사담당자 ID와 각 역량 점수를 저장
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            String competencyName = entry.getKey();
            int score = entry.getValue();

            // 역량 이름으로 competencyId 조회
            Long competencyId = competencyRepository.findIdByName(competencyName)
                    .orElseThrow(() -> new EntityNotFoundException("해당 역량이 존재하지 않습니다: " + competencyName));

            // 점수 저장 (단순 insert)
            scoreRepository.insertRawScore(managerId, competencyId, (double) score);
        }

        // 2. 인사담당자 전체 역량 점수와 등수 조회
        List<Object[]> managerRaw = scoreRepository.findAllCompetencyScoresWithRank(managerId); // [competencyId, name, score, rank]
        List<ManagerCompetencyRankDto> managerRankList = managerRaw.stream()
                .map(row -> new ManagerCompetencyRankDto(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).intValue(),
                        ((Number) row[3]).intValue()
                ))
                .toList();

        // 3. 이력서별 역량 등수 전체 조회
        List<CompetencyScoreTop3Dto> resumeRankList = getAllResumeCompetencyRanks();

        // 4. 이력서별 역량 맵 구성 (resumeId -> (competencyId -> rank))
        Map<Long, Map<Long, Integer>> resumeToCompetencyRankMap = resumeRankList.stream()
                .collect(Collectors.groupingBy(
                        CompetencyScoreTop3Dto::getResumeId,
                        Collectors.toMap(
                                CompetencyScoreTop3Dto::getCompetencyId,
                                CompetencyScoreTop3Dto::getRank
                        )
                ));

        // 5. 인사담당자 역량 맵 구성 (competencyId -> rank)
        Map<Long, Integer> managerCompetencyRankMap = managerRankList.stream()
                .collect(Collectors.toMap(
                        ManagerCompetencyRankDto::getCompetencyId,
                        ManagerCompetencyRankDto::getRank
                ));

        // 6. 유사도 계산 (등수 차이 기반 점수 합산)
        Map<Long, Integer> resumeSimilarityMap = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Integer>> entry : resumeToCompetencyRankMap.entrySet()) {
            Long resumeId = entry.getKey();
            Map<Long, Integer> resumeRanks = entry.getValue();

            int similarityScore = 0;
            for (Map.Entry<Long, Integer> managerEntry : managerCompetencyRankMap.entrySet()) {
                Long competencyId = managerEntry.getKey();
                Integer managerRank = managerEntry.getValue();

                if (resumeRanks.containsKey(competencyId)) {
                    int resumeRank = resumeRanks.get(competencyId);
                    int score = Math.max(0, 4 - Math.abs(managerRank - resumeRank)); // 등수 차가 작을수록 가산점
                    similarityScore += score;
                }
            }

            if (similarityScore > 0) {
                resumeSimilarityMap.put(resumeId, similarityScore);
            }
        }

        // 7. 유사도 상위 5개 이력서 ID 추출
        List<Long> top5ResumeIds = resumeSimilarityMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        // 8. 이력서 DTO 추출
        List<ResumeSimilarityDto> resumeDtos = resumeRepository.findResumeSimilarityDtosByIds(top5ResumeIds);

        // 9. 유사도 점수 매핑 및 정렬
        List<ResumeSimilarityDto> result = resumeDtos.stream()
                .map(dto -> new ResumeSimilarityDto(
                        dto.getResumeId(),
                        dto.getFileUrl(),
                        dto.getJobField(),
                        dto.getUserId(),
                        resumeSimilarityMap.getOrDefault(dto.getResumeId(), 0)
                ))
                .sorted((a, b) -> Integer.compare(b.getSimilarityScore(), a.getSimilarityScore()))
                .toList();

        // 10. 결과 저장 (매니저 - 유저 매칭 기록)
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new EntityNotFoundException("Manager not found"));

        List<ResumeMatchingResult> matchingResults = result.stream()
                .map(dto -> {
                    User user = userRepository.findById(dto.getUserId())
                            .orElseThrow(() -> new EntityNotFoundException("User not found"));
                    return ResumeMatchingResult.builder()
                            .manager(manager)
                            .user(user)
                            .build();
                })
                .toList();
        resumeMatchingResultRepository.saveAll(matchingResults);

        // 11. 최종 결과 반환 (인사담당자 역량 전체 등수 + 매칭 결과)
        return new ResumeMatchingResultDto(managerRankList, result);
    }

    // 모든 이력서의 역량 등수 전체 조회 메서드
    public List<CompetencyScoreTop3Dto> getAllResumeCompetencyRanks() {
        List<Object[]> result = competencyScoreRepository.findAllWithRanking(); // [resumeId, competencyId, totalScore, rank]

        return result.stream()
                .map(row -> new CompetencyScoreTop3Dto(
                        ((Number) row[0]).longValue(),
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).doubleValue(),
                        ((Number) row[3]).intValue()
                ))
                .toList();
    }



}