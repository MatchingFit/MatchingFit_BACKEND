package com.example.matching_fit.domain.score.repository;


import com.example.matching_fit.domain.score.entity.Competency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompetencyRepository extends JpaRepository<Competency, Long> {
    Optional<Competency> findByName(String name);
}
