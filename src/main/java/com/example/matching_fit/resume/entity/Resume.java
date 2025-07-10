package com.example.matching_fit.resume.entity;

import com.example.matching_fit.user.entity.User;
import org.hibernate.annotations.JdbcTypeCode;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "text_s3_url")
    private String textS3Url;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String previewText;

    @JdbcTypeCode(SqlTypes.VECTOR_FLOAT64)
    @Column(columnDefinition = "vector(768)")
    private Double[] embedding;

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
}