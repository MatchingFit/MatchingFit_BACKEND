package com.example.matching_fit.domain.resume.entity;


import com.example.matching_fit.domain.score.entity.CompetencyScore;
import com.example.matching_fit.domain.score.entity.KeywordScore;
import com.example.matching_fit.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

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
    private Long id;

    @Column(name = "file_url")
    private String fileUrl; //파일 url

    @Column(name = "text_s3_url")
    private String textS3Url; //텍스트 추출 파일

    @Lob
    @Column(columnDefinition = "TEXT")
    private String previewText; //텍스트 요약 제목

    @JdbcTypeCode(SqlTypes.VECTOR_FLOAT64)
    @Column(columnDefinition = "vector(768)")
    private Double[] embedding; //임베딩


    @Column(name = "job_field", length = 50)
    private String jobField;

//    @Column(name = "analysis_status", length = 20)
//    private String analysisStatus = "pending";
//
//    @Column(name = "retry_count")
//    private Integer retryCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    //추가
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CompetencyScore> competencyScores = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<KeywordScore> keywordScores = new ArrayList<>();
}