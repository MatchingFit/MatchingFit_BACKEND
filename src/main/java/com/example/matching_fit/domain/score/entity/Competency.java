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

    //1대다 관계 하나의 역량에 여러개의 키워드가 있을수 있음
    @OneToMany(mappedBy = "competency", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Keyword> keywords = new ArrayList<>();

}
