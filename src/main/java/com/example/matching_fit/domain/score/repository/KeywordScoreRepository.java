package com.example.matching_fit.domain.score.repository;

import com.example.matching_fit.domain.score.entity.Competency;
import com.example.matching_fit.domain.score.entity.KeywordScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeywordScoreRepository extends JpaRepository<KeywordScore, Long> {
    List<KeywordScore> findByCompetencyAndResumeIdAndUserId(Competency competency, Long resumeId, Long userId);
}
