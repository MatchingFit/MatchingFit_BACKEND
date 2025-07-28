package com.example.matching_fit.domain.score.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Category {
    BACKEND("백엔드"),
    FRONTEND("프론트엔드"),
    MOBILE("모바일 개발"),
    DESIGN("디자인"),
    CLOUD("클라우드"),
    DATA_ANALYSIS("데이터 분석"),
    BLOCKCHAIN("블록체인"),
    GAME("게임 개발"),
    MARKETING("마케팅");

    private final String label;

    Category(String label) {
        this.label = label;
    }
    @JsonValue
    public String getLabel() {
        return label;
    }
}
