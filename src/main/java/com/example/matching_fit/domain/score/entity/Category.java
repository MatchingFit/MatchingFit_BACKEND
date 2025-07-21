package com.example.matching_fit.domain.score.entity;

public enum Category {
    BACKEND("백엔드"),
    FRONTEND("프론트엔드"),
    UI_UX("UI/UX"),
    GROWTH_MARKETING("그로스마케팅");

    private final String label;

    Category(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
}
