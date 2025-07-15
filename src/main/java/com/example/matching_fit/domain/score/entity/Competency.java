package com.example.matching_fit.domain.score.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Competency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 역량명

    private Double totalScore; // 역량별 점수

    //1대다 관계 하나의 역량에 여러개의 키워드가 있을수 있음
    @OneToMany(mappedBy = "competency", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference //무한루프 방지
    @Builder.Default //기본값이 null이 안되고 초기화 시켜주는 코드
    private List<Keyword> keywords = new ArrayList<>();

    //점수를 바꾸는 메소드 추가
    public void updateTotalScore(Double totalScore){
        this.totalScore = totalScore;
    }
}
