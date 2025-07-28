package com.example.matching_fit.domain.manager.manager_competency_score.dto;

import com.example.matching_fit.domain.score.dto.ResumeSimilarityDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResumeMatchingResultDto {

    private List<ManagerCompetencyRankDto> managerRanks; // 인사담당자 역량 전체 등수
    private List<ResumeSimilarityDto> matchedResumes;    // 유사 이력서 top5
}
