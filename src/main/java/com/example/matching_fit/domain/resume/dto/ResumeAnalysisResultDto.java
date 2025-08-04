package com.example.matching_fit.domain.resume.dto;

import lombok.Data;

import java.util.List;

@Data
public class ResumeAnalysisResultDto {
    private List<ChunkAnalysis> chunkAnalyses; // 각 부분 분석 결과
    private String finalSummary;               // 최종 전체 요약 및 분석

//    // ✅ 추가된 항목별 분석 리스트
//    private List<String> strengths;        // 1. 핵심 강점
//    private List<String> weaknesses;       // 2. 보완할 점
//    private List<String> techStack;        // 3. 기술 스택 요약
//    private List<String> recommendedJobs;  // 4. 추천 직무 또는 포지션

    private List<String> summarizedSections;

    @Data
    public static class ChunkAnalysis {
        private int partNumber;
        private String originalAnalysis;
        private String summary;
    }
}
