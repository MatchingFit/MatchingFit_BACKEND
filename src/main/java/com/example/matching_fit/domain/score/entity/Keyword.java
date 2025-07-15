package com.example.matching_fit.domain.score.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Keyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String keyword; //키워드명

    @Lob
    private String embedding;

    private Double score; //키워드별 점수


    //다대1 관계 여러개의 키워드들이 하나의 역량에 속함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competency_id")
    @JsonBackReference //무한루프 방지
    private Competency competency;

    //점수를 바꾸는 메소드 추가
    public void updateScore(Double score){
        this.score = score;
    }

    //embedding 필드만 업데이트하는 메소드 추가
    public void updateEmbedding(String embedding){
        this.embedding = embedding;
    }
}
