package com.example.matching_fit.domain.score.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "keywords")
public class Keyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String keyword; //키워드명

    private Double weightScore; //가중치

    //카테고리 필드 추가(기술전문성에서 사용)
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Category category;

    //다대1 관계 여러개의 키워드들이 하나의 역량에 속함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competency_id")
    private Competency competency; //소속역량

    @JdbcTypeCode(SqlTypes.VECTOR_FLOAT64)
    @Column(columnDefinition = "vector(768)")
    private Double[] embedding;            // 임베딩 벡터

    @OneToMany(mappedBy = "keyword", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<KeywordScore> keywordScores = new ArrayList<>();

}
