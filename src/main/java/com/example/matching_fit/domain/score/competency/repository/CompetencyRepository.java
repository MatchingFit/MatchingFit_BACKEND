package com.example.matching_fit.domain.score.competency.repository;


import com.example.matching_fit.domain.score.competency.entity.Competency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetencyRepository extends JpaRepository<Competency, Long> {
}
