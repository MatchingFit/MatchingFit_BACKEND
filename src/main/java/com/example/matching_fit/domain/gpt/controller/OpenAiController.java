package com.example.matching_fit.domain.gpt.controller;

import com.example.matching_fit.domain.gpt.service.OpenAiService;
import com.example.matching_fit.domain.gpt.util.PdfUtil;
import com.example.matching_fit.domain.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/{id}/analyze")
    public ResponseEntity<?> analyzeResume(@PathVariable Long id) {
        try {
            String result = resumeService.analyzeResumeById(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("이력서 분석 실패: " + e.getMessage());
        }
    }
}
