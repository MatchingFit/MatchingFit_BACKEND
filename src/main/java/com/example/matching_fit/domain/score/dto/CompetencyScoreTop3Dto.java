package com.example.matching_fit.domain.score.dto;

public class CompetencyScoreTop3Dto {

    private Long resumeId;
    private Long competencyId;
    private Double totalScore;
    private Integer rank;

    public CompetencyScoreTop3Dto(Long resumeId, Long competencyId, Double totalScore, Integer rank) {
        this.resumeId = resumeId;
        this.competencyId = competencyId;
        this.totalScore = totalScore;
        this.rank = rank;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public Long getCompetencyId() {
        return competencyId;
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public Integer getRank() {
        return rank;
    }
}
