package com.example.matching_fit.domain.resume.entity;

import com.example.matching_fit.domain.score.entity.CompetencyScore;
import com.example.matching_fit.domain.score.entity.KeywordScore;
import com.example.matching_fit.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "resumes")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "file_url")
    private String fileUrl; //파일 url

    @Column(name = "job_field", length = 50)
    private String jobField;

    @Column(columnDefinition = "TEXT", name = "preview_text")
    private String previewText; //텍스트 요약

    @Column(name = "text_s3_url")
    private String textS3Url; //텍스트 추출 파일

    @Column(name = "pdf_url")
    private String pdfUrl;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CompetencyScore> competencyScores = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<KeywordScore> keywordScores = new ArrayList<>();

    public void updatePdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }
}