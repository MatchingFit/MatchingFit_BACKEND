package com.example.matching_fit.domain.manager.manager.controller;

import com.example.matching_fit.domain.manager.manager.dto.ManagerDto;
import com.example.matching_fit.domain.manager.manager.service.ManagerService;
import com.example.matching_fit.domain.manager.manager_competency_score.dto.ManagerCompetencyScoreRequestDto;
import com.example.matching_fit.domain.manager.manager_competency_score.service.ManagerCompetencyScoreService;
import com.example.matching_fit.global.rp.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/managers")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;
    private final ManagerCompetencyScoreService scoreService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createManager(@RequestBody ManagerDto managerDto) {
        managerService.createManager(managerDto);
        ApiResponse<?> response = ApiResponse.success(managerDto, "가입 성공!");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{managerId}")
    public ResponseEntity<ApiResponse<?>> saveScores(
            @PathVariable Long managerId,
            @RequestBody List<ManagerCompetencyScoreRequestDto> scoreDtos) {

        for (ManagerCompetencyScoreRequestDto dto : scoreDtos) {
            System.out.println("Received - Competency ID: " + dto.getCompetencyId() + ", Score: " + dto.getScore());
        }

        try {
            scoreService.saveScores(managerId, scoreDtos);
            return ResponseEntity.ok().body(ApiResponse.success(null, "입력 성공!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }
}
