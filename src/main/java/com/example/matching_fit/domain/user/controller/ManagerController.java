package com.example.matching_fit.domain.user.controller;

import com.example.matching_fit.domain.manager.manager_competency_score.dto.ManagerScoreRequestWrapperDto;
import com.example.matching_fit.domain.manager.manager_competency_score.dto.ResumeMatchingResultDto;
import com.example.matching_fit.domain.manager.manager_competency_score.service.ManagerCompetencyScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/managers")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerCompetencyScoreService scoreService;

    @PostMapping("/match")
    public ResponseEntity<ResumeMatchingResultDto> matchResumes(
            @RequestBody ManagerScoreRequestWrapperDto requestWrapper
    ) {
        ResumeMatchingResultDto result = scoreService.matchResumesByFullRanking(requestWrapper.getScores());
        return ResponseEntity.ok(result);
    }
}
