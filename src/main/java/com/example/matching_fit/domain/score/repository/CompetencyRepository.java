package com.example.matching_fit.domain.score.repository;

import com.example.matching_fit.domain.score.entity.Competency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompetencyRepository extends JpaRepository<Competency, Long> {
}
