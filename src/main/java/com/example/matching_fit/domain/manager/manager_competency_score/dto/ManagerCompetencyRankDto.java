package com.example.matching_fit.domain.manager.manager_competency_score.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ManagerCompetencyRankDto {
    private Long competencyId;
    private String competencyName;
    private int score;
    private int rank;
}
