package com.example.matching_fit.domain.score.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KeywordScoreDTO {
    private String keywordName; //키워드명
    private double score; //키워드별 점수
}
