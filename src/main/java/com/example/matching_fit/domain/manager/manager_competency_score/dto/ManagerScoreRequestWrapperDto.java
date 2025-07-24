package com.example.matching_fit.domain.manager.manager_competency_score.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ManagerScoreRequestWrapperDto {
    private Map<String, Integer> scores;
}
