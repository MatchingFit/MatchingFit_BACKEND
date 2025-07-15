package com.example.matching_fit.domain.score.service;

import com.example.matching_fit.domain.score.entity.Keyword;
import com.example.matching_fit.domain.score.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
//서버가 켜질때 자동으로 실행
public class KeywordEmbeddingService implements ApplicationRunner {
    private final KeywordRepository keywordRepository;
    private final EmbeddingService embeddingService;

    @Override
    public void run(ApplicationArguments args){
        List<Keyword> keywords = keywordRepository.findAll();
        for (Keyword keyword : keywords) {
            if (keyword.getEmbedding() == null || keyword.getEmbedding().isEmpty()){
                try {
                    List<Double> embeddingVector = embeddingService.getEmbeddingForKeyword(keyword.getKeyword());
                    String embeddingJson = embeddingService.toJson(embeddingVector);
                    keyword.updateEmbedding(embeddingJson);
                    keywordRepository.save(keyword);
                    System.out.println("저장 완료: " + keyword.getKeyword());
                }catch (Exception e){
                    System.err.println("임베딩 실패: " + keyword.getKeyword());
                    e.printStackTrace();
                }
            }
        }

    }
}
