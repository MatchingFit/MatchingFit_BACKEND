package com.example.matching_fit.domain.score.repository;


import com.example.matching_fit.domain.score.entity.Competency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetencyRepository extends JpaRepository<Competency, Long> {
}
