package com.example.matching_fit.domain.score.service;
import com.example.matching_fit.domain.manager.manager_competency_score.entity.ManagerCompetencyScore;
import com.example.matching_fit.domain.manager.manager_competency_score.repository.ManagerCompetencyScoreRepository;
import com.example.matching_fit.domain.manager.resume_matching_result.entity.ResumeMatchingResult;
import com.example.matching_fit.domain.manager.resume_matching_result.repository.ResumeMatchingResultRepository;
import com.example.matching_fit.domain.resume.entity.Resume;
import com.example.matching_fit.domain.resume.repository.ResumeRepository;
import com.example.matching_fit.domain.score.dto.CompetencyScoreTop3Dto;
import com.example.matching_fit.domain.score.dto.ResumeSimilarityDto;
import com.example.matching_fit.domain.score.repository.CompetencyScoreRepository;
import com.example.matching_fit.domain.user.entity.User;
import com.example.matching_fit.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.Manager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompetencyScoreService {

    private final CompetencyScoreRepository competencyScoreRepository;
    private final ManagerCompetencyScoreRepository scoreRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ResumeMatchingResultRepository resumeMatchingResultRepository;




}
