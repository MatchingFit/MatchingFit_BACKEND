package com.example.matching_fit.domain.resume.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ResumeSummaryDto {
    private Long resumeId;
    private String fileUrl;
    private String jobField;
    private LocalDateTime recommendedAt;

    public ResumeSummaryDto(Long resumeId, String fileUrl, String jobField, LocalDateTime recommendedAt) {
        this.resumeId = resumeId;
        this.fileUrl = fileUrl;
        this.jobField = jobField;
        this.recommendedAt = recommendedAt;
    }

}
