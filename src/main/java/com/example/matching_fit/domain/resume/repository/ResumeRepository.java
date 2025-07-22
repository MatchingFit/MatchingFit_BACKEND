package com.example.matching_fit.domain.resume.repository;

import com.example.matching_fit.domain.resume.entity.Resume;
import com.example.matching_fit.domain.score.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
}
