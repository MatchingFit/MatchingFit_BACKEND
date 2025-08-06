package com.example.matching_fit.domain.manager.resume_matching_result.controller;

import com.example.matching_fit.domain.manager.resume_matching_result.service.ResumeMatchingResultService;
import com.example.matching_fit.domain.resume.dto.ResumeSummaryDto;
import com.example.matching_fit.global.rp.ApiResponse;
import com.example.matching_fit.global.security.rq.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/matching-results")
public class ResumeMatchingResultController {

    private final ResumeMatchingResultService resumeMatchingResultService;
    private final Rq rq;

    @GetMapping("/manager")
    public ResponseEntity<ApiResponse<List<ResumeSummaryDto>>> getResumesByManager() {
        List<ResumeSummaryDto> results = resumeMatchingResultService.getRecommendedResumes(rq.getActor().getId());
        return ResponseEntity.ok(ApiResponse.success(results, "조회 성공!"));
    }
}
