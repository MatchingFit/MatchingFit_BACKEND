package com.example.matching_fit.domain.resume.repository;

import com.example.matching_fit.domain.resume.dto.ResumeTextDto;
import com.example.matching_fit.domain.resume.entity.Resume;
import com.example.matching_fit.domain.score.dto.ResumeSimilarityDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    @Query("SELECT new com.example.matching_fit.domain.resume.dto.ResumeTextDto(r.id, r.textS3Url) FROM Resume r WHERE r.id = :id")
    Optional<ResumeTextDto> findResumeTextDtoById(@Param("id") Long id);

    @Query("select new com.example.matching_fit.domain.score.dto.ResumeSimilarityDto(r.id, r.fileUrl, r.jobField, r.user.id) from Resume r where r.id in :ids")
    List<ResumeSimilarityDto> findResumeSimilarityDtosByIds(@Param("ids") List<Long> ids);
}
