package com.example.matching_fit.domain.score.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KeywordEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String keyword;

    @Lob
    private String embeddingJson;

    public List<Double> getEmbeddingVectorAsList() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(embeddingJson, new TypeReference<List<Double>>() {});
        } catch (Exception e) {
            throw new RuntimeException("키워드 임베딩 실패", e);
        }
    }
}
