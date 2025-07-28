package com.example.matching_fit.domain.score.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResumeSimilarityDto {
    private Long resumeId;
    private String fileUrl;
    private String jobField;
    private Long userId;
    private int similarityScore;

    // JPQL에서 사용하는 생성자 (similarityScore는 나중에 setter로 설정)
    public ResumeSimilarityDto(Long resumeId, String fileUrl, String jobField, Long userId) {
        this.resumeId = resumeId;
        this.fileUrl = fileUrl;
        this.jobField = jobField;
        this.userId = userId;
    }
}
