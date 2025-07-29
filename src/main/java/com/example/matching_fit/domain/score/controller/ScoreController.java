package com.example.matching_fit.domain.score.controller;

import com.example.matching_fit.domain.score.dto.CompetencyScoreDTO;
import com.example.matching_fit.domain.score.dto.ScoreRequestDTO;
import com.example.matching_fit.domain.score.service.ElasticsearchService;
import com.example.matching_fit.domain.score.service.ScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/score")
@Slf4j
@RequiredArgsConstructor
public class ScoreController {
    private final ScoreService scoreService;
    private final ElasticsearchService elasticsearchService;

    @PostMapping("/total")
    public ResponseEntity<List<CompetencyScoreDTO>> getScore(@RequestBody ScoreRequestDTO scoreRequestDTO) {
        List<CompetencyScoreDTO> competencyScoreDTO =  elasticsearchService.getAllCosineScoreDTOs(scoreRequestDTO.getResumeId(), scoreRequestDTO.getEmbedding());
        return ResponseEntity.ok(competencyScoreDTO);
    }

    // 전체 이력 조회(KeywordScore 기준, 역량별 계층화)
    @GetMapping("/history")
    public List<CompetencyScoreDTO> getHistoryScore(){
        return scoreService.findHistoryScore();
    }

    // 상세 이력 조회(ResumeId로, KeywordScore 기준)
    @GetMapping("/history/detail")
    public List<CompetencyScoreDTO> getHistoryDetailScore(@RequestParam Long resumeId) {
        return scoreService.findHistoryDetailScore(resumeId);
    }

}