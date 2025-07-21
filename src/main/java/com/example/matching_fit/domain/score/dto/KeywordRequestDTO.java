package com.example.matching_fit.domain.score.dto;

import com.example.matching_fit.domain.score.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KeywordRequestDTO {
    private Category category;
}
