package com.example.matching_fit.domain.score.controller;

import com.example.matching_fit.domain.resume.dto.ResumeScoreRequestDTO;
import com.example.matching_fit.domain.score.dto.CompetencyScoreDTO;
import com.example.matching_fit.domain.score.entity.Competency;
import com.example.matching_fit.domain.score.service.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/score")
@RequiredArgsConstructor
public class ScoreController {
    private final ScoreService scoreService;

    //이력서 점수결과
    @PostMapping("/total")
    public List<CompetencyScoreDTO> getScore(@RequestBody ResumeScoreRequestDTO resumeScoreRequestDTO) {
        return scoreService.sumScore(resumeScoreRequestDTO.getResumeId());
    }

    //이력서 조회(전체)
    @GetMapping("/history")
    public List<CompetencyScoreDTO> getHistoryScore(){
        return scoreService.findHistoryScore();
    }

    //이력서 조회 (상세 조회 / 사용자+이력서)
    @GetMapping("/history/detail")
    public List<CompetencyScoreDTO> getHistoryDetailScore(
            @RequestParam Long resumeId
    ) {
        return scoreService.findHistoryDetailScore(resumeId);
    }

}
