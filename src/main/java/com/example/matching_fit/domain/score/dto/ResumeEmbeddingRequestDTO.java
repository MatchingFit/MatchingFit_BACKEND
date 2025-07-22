package com.example.matching_fit.domain.score.dto;

import lombok.Getter;

import java.util.List;

@Getter
//api 요청 dto, 파이썬에서 변환된 데이터를 받는곳
public class ResumeEmbeddingRequestDTO {
    private Long userId;
    private Long resumeId;
}
