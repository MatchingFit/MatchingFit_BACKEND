package com.example.matching_fit.domain.gpt.controller;

import com.example.matching_fit.domain.gpt.service.OpenAiService;
import com.example.matching_fit.domain.resume.dto.ResumeAnalysisResultDto;
import com.example.matching_fit.domain.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/gpt")
@RequiredArgsConstructor
public class OpenAiController {

    private final OpenAiService openAiService;
    private final ResumeService resumeService;

    @PostMapping("/chat")
    public String chat(@RequestBody String prompt) {
        return openAiService.getChatCompletion(prompt);
    }

    @GetMapping("/resumes/{id}/analyze")
    public ResponseEntity<ResumeAnalysisResultDto> analyzeResume(@PathVariable Long id) throws Exception {
        ResumeAnalysisResultDto result = resumeService.analyzeResumeById(id);
        return ResponseEntity.ok(result);
    }
}
