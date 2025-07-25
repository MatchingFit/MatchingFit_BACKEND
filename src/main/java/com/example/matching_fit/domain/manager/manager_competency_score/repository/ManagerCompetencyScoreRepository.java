package com.example.matching_fit.domain.manager.manager_competency_score.repository;

import com.example.matching_fit.domain.manager.manager_competency_score.entity.ManagerCompetencyScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ManagerCompetencyScoreRepository extends JpaRepository<ManagerCompetencyScore, Long> {
    @Query("""
        SELECT mcs.competency.id, mcs.competency.name, mcs.competencyScore,
               RANK() OVER (ORDER BY mcs.competencyScore DESC) AS rank
        FROM ManagerCompetencyScore mcs
        WHERE mcs.manager.id = :managerId
        """)
    List<Object[]> findAllCompetencyScoresWithRank(@Param("managerId") Long managerId);

    @Modifying
    @Query(value = "INSERT INTO manager_competency_scores (manager_id, competency_id, competency_score) " +
            "VALUES (:managerId, :competencyId, :score)", nativeQuery = true)
    void insertRawScore(@Param("managerId") Long managerId,
                        @Param("competencyId") Long competencyId,
                        @Param("score") Double score);
}
