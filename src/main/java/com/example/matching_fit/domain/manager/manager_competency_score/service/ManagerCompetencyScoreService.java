package com.example.matching_fit.domain.manager.manager_competency_score.service;

import com.example.matching_fit.domain.manager.manager.entity.Manager;
import com.example.matching_fit.domain.manager.manager.repository.ManagerRepository;
import com.example.matching_fit.domain.manager.manager_competency_score.dto.ManagerCompetencyScoreRequestDto;
import com.example.matching_fit.domain.manager.manager_competency_score.entity.ManagerCompetencyScore;
import com.example.matching_fit.domain.manager.manager_competency_score.repository.ManagerCompetencyScoreRepository;
import com.example.matching_fit.domain.score.competency.entity.Competency;
import com.example.matching_fit.domain.score.competency.repository.CompetencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerCompetencyScoreService {

    private final ManagerRepository managerRepository;
    private final CompetencyRepository competencyRepository;
    private final ManagerCompetencyScoreRepository scoreRepository;

    @Transactional
    public void saveScores(Long managerId, List<ManagerCompetencyScoreRequestDto> scoreDtos) {
        // 매니저 조회
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 매니저가 존재하지 않습니다. id=" + managerId));

        // 각 점수 저장
        for (ManagerCompetencyScoreRequestDto dto : scoreDtos) {
            // 역량 조회
            Competency competency = competencyRepository.findById(dto.getCompetencyId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 역량이 존재하지 않습니다. id=" + dto.getCompetencyId()));

            // 점수 엔티티 생성
            ManagerCompetencyScore score = ManagerCompetencyScore.builder()
                    .manager(manager)
                    .competency(competency)
                    .competencyScore(dto.getScore())
                    .build();

            // 저장
            scoreRepository.save(score);
        }
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTop3Competencies(Long managerId) {
        List<Object[]> results = scoreRepository.findTop3CompetencyScoresWithRank(managerId);

        return results.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("competencyId", ((Number)row[0]).longValue());
                    map.put("competencyScore", ((Number)row[1]).intValue());
                    map.put("rank", ((Number)row[2]).intValue());
                    return map;
                })
                .collect(Collectors.toList());
    }
}