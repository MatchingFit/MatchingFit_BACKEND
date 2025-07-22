package com.example.matching_fit.domain.score.util;

public class EmbeddingUtil {
    //⭐️ 두 벡터(Double[])의 코사인 유사도 계산
    public static double cosineSimilarity(Double[] vec1, Double[] vec2) {
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < vec1.length; i++) {
            dot += vec1[i] * vec2[i];
            normA += vec1[i] * vec1[i];
            normB += vec2[i] * vec2[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
