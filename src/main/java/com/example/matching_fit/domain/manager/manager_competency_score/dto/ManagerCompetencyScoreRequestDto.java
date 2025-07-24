package com.example.matching_fit.domain.manager.manager_competency_score.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerCompetencyScoreRequestDto {
    private String competencyName; // 변경됨
    private int score;
}
