package com.example.matching_fit.domain.resume.controller;

import com.example.matching_fit.domain.resume.dto.ResumePdfDto;
import com.example.matching_fit.domain.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {
    private final ResumeService resumeService;

    @PostMapping("/update/pdf")
    public ResponseEntity<Void> analyzeResume(@RequestBody ResumePdfDto request) {
        resumeService.updatePdfUrl(request.getResumeId(), request.getPdfUrl());
        return ResponseEntity.ok().build();
    }
}