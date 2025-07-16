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

    private Double weightScore; //가중치

    //다대1 관계 여러개의 키워드들이 하나의 역량에 속함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competency_id")
    private Competency competency;

    //키워드별로 중요도 나누기
    // (예시 키워드별 점수가 1점씩이엿다면 이코드를 사용하면 2 0.5 0.5로 변환가능)
    public void updateWeightScore(Double weightScore){
        this.weightScore = weightScore;
    }
}
