package com.example.matching_fit.domain.score.service;

import com.example.matching_fit.domain.score.dto.CompetencyScoreTop3Dto;
import com.example.matching_fit.domain.score.repository.CompetencyScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompetencyScoreService {

    private final CompetencyScoreRepository competencyScoreRepository;
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

}
