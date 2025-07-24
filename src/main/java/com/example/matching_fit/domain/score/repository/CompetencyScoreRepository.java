package com.example.matching_fit.domain.score.repository;

import com.example.matching_fit.domain.score.entity.CompetencyScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetencyScoreRepository extends JpaRepository<CompetencyScore, Long> {

    @Query(value = """
        SELECT
            cs.resume_id AS resumeId,
            cs.competency_id AS competencyId,
            cs.total_score AS totalScore,
            ROW_NUMBER() OVER (PARTITION BY cs.resume_id ORDER BY cs.total_score DESC) AS rank
        FROM competencyscores cs
    """, nativeQuery = true)
    List<Object[]> findAllWithRanking();
}
