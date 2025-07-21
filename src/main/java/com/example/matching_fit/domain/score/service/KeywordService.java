package com.example.matching_fit.domain.score.service;

import com.example.matching_fit.domain.score.dto.KeywordResponseDTO;
import com.example.matching_fit.domain.score.entity.Category;
import com.example.matching_fit.domain.score.entity.Keyword;
import com.example.matching_fit.domain.score.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordRepository keywordRepository;

    public List<KeywordResponseDTO> getKeywordsByCategory(Category category) {
        List<Keyword> keywords = keywordRepository.findByCategory(category);
        return keywords.stream()
                .map(k -> new KeywordResponseDTO(k.getKeyword(), k.getCategory()))
                .toList();
    }
}
