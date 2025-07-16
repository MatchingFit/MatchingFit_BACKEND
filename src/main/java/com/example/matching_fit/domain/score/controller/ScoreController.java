package com.example.matching_fit.domain.score.controller;

import com.example.matching_fit.domain.score.dto.CompetencyScoreDTO;
import com.example.matching_fit.domain.score.dto.ResumeEmbeddingRequestDTO;
import com.example.matching_fit.domain.score.entity.Competency;
import com.example.matching_fit.domain.score.service.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/resume/score")
@RequiredArgsConstructor
public class ScoreController {
    private final ScoreService scoreService;

    //이력서 점수결과
    //이력서 id, 유저 id가 추가적으로 좋을듯 현재는 둘다 추가해본거같음
    @PostMapping("/sum/{userId}/{resumeId}")
    public List<CompetencyScoreDTO> getScore(
            @PathVariable Long userId,
            @PathVariable Long resumeId,
            @RequestBody ResumeEmbeddingRequestDTO resumeEmbeddingRequestDTO
    ){
        if (!userId.equals(resumeEmbeddingRequestDTO.getUserId()) || !resumeId.equals(resumeEmbeddingRequestDTO.getResumeId()))
        {
            throw new IllegalArgumentException("userId/resumeId 값이 일치하지않습니다.");
        }
        return scoreService.sumScore(userId, resumeId,resumeEmbeddingRequestDTO);
    }

    //이력서 조회(전체)
//    @GetMapping("/history")
//    public List<CompetencyScoreDTO> getHistoryScore(){
//        return scoreService.findHistoryScore();
//    }
//
//    //이력서 조회 (상세 조회)
//    @GetMapping("/history/{userId}/{resumeId}")
//    public List<CompetencyScoreDTO> getHistoryDetailScore(
//            @PathVariable Long userId,
//            @PathVariable Long resumeId
//    ) {
//        return scoreService.findHistoryDetailScore(userId, resumeId);
//    }

}
