package com.example.matching_fit.domain.score.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CompetencyScoreDTO {
    private String competencyName; //역량명
    private double totalScore; // 역량별 총점수
    private List<KeywordScoreDTO> keywordScoreDTOS; //키워드별 점수리스트
}
