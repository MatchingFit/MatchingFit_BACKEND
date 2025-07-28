package com.example.matching_fit.domain.resume.dto;

import lombok.Data;

import java.util.List;

@Data
public class ResumeAnalysisResultDto {
    private List<ChunkAnalysis> chunkAnalyses;
    private String finalSummary;

    @Data
    public static class ChunkAnalysis {
        private int partNumber;
        private String originalAnalysis;
        private String summary;
    }
}
