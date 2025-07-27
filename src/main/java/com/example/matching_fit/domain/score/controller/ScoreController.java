package com.example.matching_fit.domain.score.controller;

import com.example.matching_fit.domain.resume.dto.ResumeScoreRequestDTO;
import com.example.matching_fit.domain.score.dto.CompetencyScoreDTO;
import com.example.matching_fit.domain.score.dto.KeywordScoreDTO;
import com.example.matching_fit.domain.score.dto.ResumeIdRequestDTO;
import com.example.matching_fit.domain.score.entity.Competency;
import com.example.matching_fit.domain.score.service.ElasticsearchService;
import com.example.matching_fit.domain.score.service.ScoreService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/score")
@RequiredArgsConstructor
@Tag(name = "Score", description = "점수 결과 관련 API")
@Slf4j
public class ScoreController {
    private final ScoreService scoreService;
    private final ElasticsearchService elasticsearchService;

    //이력서 점수결과
    @PostMapping("/total")
    public List<KeywordScoreDTO> getScore(@RequestBody ResumeIdRequestDTO resumeIdRequestDTO) {
        return elasticsearchService.getAllCosineScoreDTOs(resumeIdRequestDTO.getResumId());
    }

    // 전체 이력 조회(KeywordScore 기준, 역량별 계층화)
    @GetMapping("/history")
    public List<CompetencyScoreDTO> getHistoryScore(){
        return scoreService.findHistoryScore();
    }

    // 상세 이력 조회(ResumeId로, KeywordScore 기준)
    @GetMapping("/history/detail")
    public List<CompetencyScoreDTO> getHistoryDetailScore(
            @RequestParam Long resumeId
    ) {
        return scoreService.findHistoryDetailScore(resumeId);
    }

}
