package com.example.matching_fit.domain.score.repository;

import com.example.matching_fit.domain.score.entity.Competency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.matching_fit.domain.score.entity.Competency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompetencyRepository extends JpaRepository<Competency, Long> {
    Optional<Competency> findByName(String name);
    @Query("SELECT c.id FROM Competency c WHERE c.name = :name")
    Optional<Long> findIdByName(@Param("name") String name);

}
