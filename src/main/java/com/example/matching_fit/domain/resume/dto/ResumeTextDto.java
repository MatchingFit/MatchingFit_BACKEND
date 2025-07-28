package com.example.matching_fit.domain.resume.dto;

public class ResumeTextDto {
    private final Long id;
    private final String textS3Url;

    public ResumeTextDto(Long id, String textS3Url) {
        this.id = id;
        this.textS3Url = textS3Url;
    }

    public Long getId() {
        return id;
    }

    public String getTextS3Url() {
        return textS3Url;
    }
}
