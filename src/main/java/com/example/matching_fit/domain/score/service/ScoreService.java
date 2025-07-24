package com.example.matching_fit.domain.score.service;

import com.example.matching_fit.domain.resume.entity.Resume;
import com.example.matching_fit.domain.resume.repository.ResumeRepository;
import com.example.matching_fit.domain.score.dto.CompetencyScoreDTO;
import com.example.matching_fit.domain.score.dto.KeywordScoreDTO;
import com.example.matching_fit.domain.score.entity.*;
import com.example.matching_fit.domain.score.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreService {
    private final KeywordScoreRepository keywordScoreRepository;
    private final ResumeRepository resumeRepository;

    // 전체 점수 이력 조회 - KeywordScore만 사용, 역량별 그룹핑
    public List<CompetencyScoreDTO> findHistoryScore() {
        List<KeywordScore> keywordScores = keywordScoreRepository.findAll();

        // 역량(competency)별로 그룹핑
        Map<String, List<KeywordScore>> byCompetency = keywordScores.stream()
                .collect(Collectors.groupingBy(ks -> ks.getCompetency().getName()));

        List<CompetencyScoreDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<KeywordScore>> entry : byCompetency.entrySet()) {
            String competencyName = entry.getKey();
            List<KeywordScore> scores = entry.getValue();

            double totalScore = scores.stream()
                    .mapToDouble(ks -> ks.getScore() != null ? ks.getScore() : 0.0)
                    .sum();

            List<KeywordScoreDTO> keywordDTOs = scores.stream()
                    .map(ks -> KeywordScoreDTO.builder()
                            .keywordName(ks.getKeyword().getKeyword())
                            .score(ks.getScore() != null ? ks.getScore() : 0.0)
                            .category(ks.getKeyword().getCategory())
                            .userId(ks.getResume().getUser().getId())      // 추가
                            .userName(ks.getResume().getUser().getName())  // 추가
                            .build())
                    .collect(Collectors.toList());

            result.add(CompetencyScoreDTO.builder()
                    .competencyName(competencyName)
                    .totalScore(totalScore)
                    .keywordScoreDTOS(keywordDTOs)
                    .build());
        }
        return result;
    }

    // 상세 조회(ResumeId) - KeywordScore만 사용, 역량별 그룹핑
    public List<CompetencyScoreDTO> findHistoryDetailScore(Long resumeId) {
        List<KeywordScore> keywordScores = keywordScoreRepository.findByResumeId(resumeId);

        Map<String, List<KeywordScore>> byCompetency = keywordScores.stream()
                .collect(Collectors.groupingBy(ks -> ks.getCompetency().getName()));

        List<CompetencyScoreDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<KeywordScore>> entry : byCompetency.entrySet()) {
            String competencyName = entry.getKey();
            List<KeywordScore> scores = entry.getValue();

            double totalScore = scores.stream()
                    .mapToDouble(ks -> ks.getScore() != null ? ks.getScore() : 0.0)
                    .sum();

            List<KeywordScoreDTO> keywordDTOs = scores.stream()
                    .map(ks -> KeywordScoreDTO.builder()
                            .keywordName(ks.getKeyword().getKeyword())
                            .score(ks.getScore() != null ? ks.getScore() : 0.0)
                            .category(ks.getKeyword().getCategory())
                            .userId(ks.getResume().getUser().getId())      // 추가
                            .userName(ks.getResume().getUser().getName())  // 추가
                            .build())
                    .collect(Collectors.toList());

            result.add(CompetencyScoreDTO.builder()
                    .competencyName(competencyName)
                    .totalScore(totalScore)
                    .keywordScoreDTOS(keywordDTOs)
                    .build());
        }
        return result;
    }
}