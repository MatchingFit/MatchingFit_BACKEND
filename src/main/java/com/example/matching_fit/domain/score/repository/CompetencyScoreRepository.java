package com.example.matching_fit.domain.score.repository;

import com.example.matching_fit.domain.score.entity.CompetencyScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompetencyScoreRepository extends JpaRepository<CompetencyScore, Long> {
    List<CompetencyScore> findByUserIdAndResumeId(Long userId, Long resumeId);
}
