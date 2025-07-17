package com.example.matching_fit.domain.gpt.controller;

import com.example.matching_fit.domain.gpt.service.GptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gpt")
@RequiredArgsConstructor
public class OpenAiController {

    private final GptService openAiService;

    @PostMapping("/chat")
    public String chat(@RequestBody String prompt) {
        return openAiService.getChatCompletion(prompt);
    }
}
