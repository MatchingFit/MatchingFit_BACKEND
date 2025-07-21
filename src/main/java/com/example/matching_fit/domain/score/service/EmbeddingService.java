package com.example.matching_fit.domain.score.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
// 점수계산, 파이선에서 넘어온 임베딩 문자열을 실수로 변환 후 코사인 유사도를 사용해서 실수 점수로 계산
public class EmbeddingService {
    private final ObjectMapper objectMapper = new ObjectMapper();

    //⭐️ 임베딩 문자열을 List<Double>로 변환
    public List<Double> parseEmbedding(String embeddingJSON) {
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            return objectMapper.readValue(embeddingJSON, new TypeReference<List<Double>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("임베딩 파싱 실패", e);
        }
    }
    //⭐️ 코사인 유사도 계산 함수
    // 이건삭제해도 될꺼같음 사유는 엘라스틱서치가 해주니깐
    public double cosineSimilarity(List<Double> vec1, List<Double> vec2) {
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < vec1.size(); i++) {
            dot += vec1.get(i) * vec2.get(i);
            normA += Math.pow(vec1.get(i), 2);
            normB += Math.pow(vec2.get(i), 2);
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
