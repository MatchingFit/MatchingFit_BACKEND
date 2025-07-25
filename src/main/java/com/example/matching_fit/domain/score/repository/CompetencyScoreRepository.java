package com.example.matching_fit.domain.score.repository;

import com.example.matching_fit.domain.score.entity.CompetencyScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompetencyScoreRepository extends JpaRepository<CompetencyScore, Long> {

    @Query("""
    SELECT cs.resume.id, cs.competency.id, cs.totalScore, 
           RANK() OVER (PARTITION BY cs.resume.id ORDER BY cs.totalScore DESC) AS rank
    FROM CompetencyScore cs
    """)
    List<Object[]> findAllWithRanking();

    @Query("SELECT cs FROM CompetencyScore cs WHERE cs.resume.id = :resumeId AND cs.competency.id = :competencyId")
    Optional<CompetencyScore> findByResumeIdAndCompetencyId(@Param("resumeId") Long resumeId, @Param("competencyId") Long competencyId);

    @Modifying
    @Query(value = "INSERT INTO competencyscores (resume_id, competency_id, total_score) VALUES (:resumeId, :competencyId, :score)", nativeQuery = true)
    void insertRawScore(@Param("resumeId") Long resumeId,
                        @Param("competencyId") Long competencyId,
                        @Param("score") Double score);


}
