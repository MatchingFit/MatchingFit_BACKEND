package com.example.matching_fit.domain.resume.controller;

import com.example.matching_fit.domain.resume.dto.ResumeAnalysisResultDto;
import com.example.matching_fit.domain.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gpt")
@RequiredArgsConstructor
public class OpenAiController {
    private final ResumeService resumeService;

    @GetMapping("/resumes/{id}/analyze")
    public ResponseEntity<ResumeAnalysisResultDto> analyzeResume(@PathVariable Long id) throws Exception {
        ResumeAnalysisResultDto result = resumeService.analyzeResumeById(id);
        return ResponseEntity.ok(result);
    }
}
