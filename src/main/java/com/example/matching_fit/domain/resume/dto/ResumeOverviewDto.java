package com.example.matching_fit.domain.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ResumeOverviewDto {
    private String name;
    private String fileUrl;
    private String jobField;
    private LocalDateTime updatedAt;
    private String pdfUrl;
}
