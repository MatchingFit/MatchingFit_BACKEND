package com.example.matching_fit.domain.score.dto;

import lombok.Data;

import java.util.List;

@Data
public class ScoreRequestDTO {
    private Long resumeId;
    private List<Double> embedding;
}