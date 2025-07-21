package com.example.matching_fit.domain.score.controller;

import com.example.matching_fit.domain.score.dto.KeywordRequestDTO;
import com.example.matching_fit.domain.score.dto.KeywordResponseDTO;
import com.example.matching_fit.domain.score.service.KeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {
    private final KeywordService keywordService;

    @PostMapping("/select")
    public List<KeywordResponseDTO> getKeywordsByCategory(
            @RequestBody KeywordRequestDTO keywordRequestDTO) {
        return keywordService.getKeywordsByCategory(keywordRequestDTO.getCategory());
    }
}
