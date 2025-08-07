package com.example.matching_fit.domain.resume.controller;

import com.example.matching_fit.domain.resume.dto.ResumeOverviewDto;
import com.example.matching_fit.domain.resume.dto.ResumePdfDto;
import com.example.matching_fit.domain.resume.dto.ResumeSummaryDto;
import com.example.matching_fit.domain.resume.service.ResumeService;
import com.example.matching_fit.global.security.rq.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {
    private final ResumeService resumeService;
    private final Rq rq;

    @PostMapping("/update/pdf")
    public ResponseEntity<Void> analyzeResume(@RequestBody ResumePdfDto request) {
        resumeService.updatePdfUrl(request.getResumeId(), request.getPdfUrl());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<ResumeOverviewDto>> getMyResumes() {
        return ResponseEntity.ok(resumeService.getResumesForCurrentUser(rq.getActor().getId()));
    }
}