package com.example.matching_fit.domain.score.dto;

import com.example.matching_fit.domain.score.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KeywordResponseDTO {
    private String keywordName;
    private Category category;
}
