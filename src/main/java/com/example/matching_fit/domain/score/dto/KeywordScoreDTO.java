package com.example.matching_fit.domain.score.dto;

import com.example.matching_fit.domain.score.entity.Category;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KeywordScoreDTO {
    private String keywordName; //키워드명
    private double score; //키워드별 점수
    private Category category; //카테고리 추가
    private Long userId;     //사용자 아이디 추가
    private String userName; //사용자 이름 추가
}
