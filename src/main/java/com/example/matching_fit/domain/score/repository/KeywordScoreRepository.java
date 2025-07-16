package com.example.matching_fit.domain.score.repository;

import com.example.matching_fit.domain.score.entity.KeywordScore;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordScoreRepository extends JpaRepository<KeywordScore, Long> {
}
