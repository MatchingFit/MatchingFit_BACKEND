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
            @RequestBody ResumeEmbeddingRequestDTO resumeEmbeddingRequestDTO
    ){
        return scoreService.sumScore(resumeEmbeddingRequestDTO);
    }
    //인증로직은 없애이유 - 비교군이 무조건 똑같을 꺼고, true로직일꺼기때문에 인증을 하나마나 일거같다
    //유저아이디, 이력서아이디를 삭제한이유가 중복되는거였고,

    //이력서 조회(전체)
    @GetMapping("/history")
    public List<CompetencyScoreDTO> getHistoryScore(){
        return scoreService.findHistoryScore();
    }

    //이력서 조회 (상세 조회 / 사용자+이력서)
    @GetMapping("/history/{userId}/{resumeId}")
    public List<CompetencyScoreDTO> getHistoryDetailScore(
            @RequestParam Long userId,
            @RequestParam Long resumeId
    ) {
        return scoreService.findHistoryDetailScore(userId, resumeId);
    }

}
