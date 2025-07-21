package com.example.matching_fit.domain.gpt.controller;

import com.example.matching_fit.domain.gpt.service.OpenAiService;
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

    @GetMapping("/{id}/analyze")
    public ResponseEntity<String> analyzeResume(@PathVariable Long id) {
        try {
            String result = resumeService.analyzeResumeById(id);
            return ResponseEntity.ok(result);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
