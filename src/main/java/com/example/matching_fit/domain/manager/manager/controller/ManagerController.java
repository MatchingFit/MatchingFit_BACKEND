package com.example.matching_fit.domain.manager.manager.controller;

import com.example.matching_fit.domain.manager.manager.dto.ManagerDto;
import com.example.matching_fit.domain.manager.manager.service.ManagerService;
import com.example.matching_fit.domain.manager.manager_competency_score.dto.ManagerScoreRequestWrapperDto;
import com.example.matching_fit.domain.manager.manager_competency_score.service.ManagerCompetencyScoreService;
import com.example.matching_fit.domain.score.dto.ResumeSimilarityDto;
import com.example.matching_fit.domain.score.service.CompetencyScoreService;
import com.example.matching_fit.global.rp.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/managers")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;
    private final ManagerCompetencyScoreService scoreService;
    private final CompetencyScoreService competencyScoreService;


    @PostMapping
    public ResponseEntity<ApiResponse<?>> createManager(@RequestBody ManagerDto managerDto) {
        managerService.createManager(managerDto);
        ApiResponse<?> response = ApiResponse.success(managerDto, "가입 성공!");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{managerId}")
    public ResponseEntity<ApiResponse<?>> saveScores(
            @PathVariable Long managerId,
            @RequestBody ManagerScoreRequestWrapperDto requestDto) {

        try {
            scoreService.saveScoresByName(managerId, requestDto.getScores());
            return ResponseEntity.ok().body(ApiResponse.success(null, "입력 성공!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }

    @GetMapping("/{managerId}/top3")
    public ResponseEntity<List<Map<String, Object>>> getTop3Competencies(@PathVariable Long managerId) {
        List<Map<String, Object>> top3 = scoreService.getTop3Competencies(managerId);
        return ResponseEntity.ok(top3);
    }

    @GetMapping("/similar-resumes/top5")
    public ResponseEntity<List<ResumeSimilarityDto>> getTop5SimilarResumes(@RequestParam Long managerId) {
        List<ResumeSimilarityDto> result = competencyScoreService.getTop5SimilarResumesWithInfo(managerId);
        return ResponseEntity.ok(result);
    }
}
