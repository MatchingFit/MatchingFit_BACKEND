package com.example.matching_fit.domain.score.repository;

import com.example.matching_fit.domain.score.entity.KeywordEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KeywordEmbeddingRepository extends JpaRepository<KeywordEmbedding, Long> {
    Optional<KeywordEmbedding> findByKeyword(String keyword);  //키워드명으로 조회

}