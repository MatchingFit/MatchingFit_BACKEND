package com.example.matching_fit.domain.score.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
public class ResumeEmbedding {
    @Id
    private Long resumeId;

    @Lob
    private String embeddingJson;

    public void updateEmbedding(String newEmbeddingJson){
        this.embeddingJson = newEmbeddingJson;
    }

    public List<Double> getEmbeddingVectorAsList(){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(embeddingJson, new TypeReference<List<Double>>() {
            });
        }catch (Exception e) {
            throw new RuntimeException("이력서 임베딩 실패", e);
        }
    }
}
