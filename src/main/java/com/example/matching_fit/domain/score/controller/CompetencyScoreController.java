package com.example.matching_fit.domain.score.controller;

import com.example.matching_fit.domain.score.dto.CompetencyScoreTop3Dto;
import com.example.matching_fit.domain.score.service.CompetencyScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/competencyscores")
public class CompetencyScoreController {

    private final CompetencyScoreService competencyScoreService;

}
