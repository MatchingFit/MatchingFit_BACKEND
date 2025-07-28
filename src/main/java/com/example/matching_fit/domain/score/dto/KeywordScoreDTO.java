package com.example.matching_fit.domain.score.dto;

import com.example.matching_fit.domain.score.entity.Category;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KeywordScoreDTO {
    private String keywordName;
    private double score;
    private Category category;
    private Long userId;
    private String userName;
}
