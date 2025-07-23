package com.example.matching_fit.domain.manager.manager_competency_score.repository;

import com.example.matching_fit.domain.manager.manager_competency_score.entity.ManagerCompetencyScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ManagerCompetencyScoreRepository extends JpaRepository<ManagerCompetencyScore, Long> {
    @Query(value = """
        SELECT competency_id, competency_score, rank FROM (
            SELECT competency_id, competency_score,
            RANK() OVER (ORDER BY competency_score DESC) AS rank
            FROM manager_competency_scores
            WHERE manager_id = :managerId
        ) sub
        ORDER BY rank
        LIMIT 3
        """, nativeQuery = true)
    List<Object[]> findTop3CompetencyScoresWithRank(@Param("managerId") Long managerId);
}
