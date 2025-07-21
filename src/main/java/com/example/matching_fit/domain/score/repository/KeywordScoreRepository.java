package com.example.matching_fit.domain.score.repository;

import com.example.matching_fit.domain.score.entity.KeywordScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeywordScoreRepository extends JpaRepository<KeywordScore, Long> {
    List<KeywordScore> findByCompetencyIdAndResumeIdAndUserId(Long competencyId, Long resumeId, Long userId);
}
