package com.example.matching_fit.domain.manager.resume_matching_result.service;

import com.example.matching_fit.domain.manager.resume_matching_result.repository.ResumeMatchingResultRepository;
import com.example.matching_fit.domain.resume.dto.ResumeSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ResumeMatchingResultService {

    private final ResumeMatchingResultRepository resumeMatchingResultRepository;

    public List<ResumeSummaryDto> getRecommendedResumes(Long managerId) {
        return resumeMatchingResultRepository.findResumesByManagerId(managerId);
    }
}
