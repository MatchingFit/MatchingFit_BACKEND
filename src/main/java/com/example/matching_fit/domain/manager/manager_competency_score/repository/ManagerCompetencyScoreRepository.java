package com.example.matching_fit.domain.manager.manager_competency_score.repository;

import com.example.matching_fit.domain.manager.manager_competency_score.entity.ManagerCompetencyScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ManagerCompetencyScoreRepository extends JpaRepository<ManagerCompetencyScore, Long> {
    @Query(value = """
    SELECT t.competency_id, c.name, t.max_score,
           RANK() OVER (ORDER BY t.max_score DESC) AS rank
    FROM (
        SELECT mcs.competency_id, MAX(mcs.competency_score) AS max_score
        FROM manager_competency_scores mcs
        WHERE mcs.manager_id = :managerId
        GROUP BY mcs.competency_id
    ) t
    JOIN competencies c ON t.competency_id = c.id
    """, nativeQuery = true)
    List<Object[]> findAllCompetencyScoresWithRank(@Param("managerId") Long managerId);

    @Modifying
    @Query(value = "INSERT INTO manager_competency_scores (manager_id, competency_id, competency_score) " +
            "VALUES (:managerId, :competencyId, :score)", nativeQuery = true)
    void insertRawScore(@Param("managerId") Long managerId,
                        @Param("competencyId") Long competencyId,
                        @Param("score") Double score);
}
