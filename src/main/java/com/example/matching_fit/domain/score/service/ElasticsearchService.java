package com.example.matching_fit.domain.score.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.matching_fit.domain.score.entity.KeywordEmbedding;
import com.example.matching_fit.domain.score.entity.ResumeEmbedding;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ElasticsearchService {
    private final ElasticsearchClient elasticsearchClient;

    public double getCosineScore(ResumeEmbedding resume, KeywordEmbedding keyword) {
        List<Double> resumeVec = resume.getEmbeddingVectorAsList();
        List<Float> resumeFloatList = resumeVec.stream()
                .map(Double::floatValue)
                .collect(Collectors.toList());


        try {
            // knn 쿼리 예시: 이력서 벡터가 쿼리 벡터,
            // 인덱스 내 문서들의 embedding 필드와 유사도 비교
            SearchRequest searchRequest = SearchRequest.of(b -> b
                    .index("index") //엘라스틱서치 인덱스명 설정, 추후 변경필요(실제 인덱스명으로)
                    .knn(knn -> knn
                            .field("embedding")
                            .queryVector(resumeFloatList)
                            .k(1)
                    )
            );

            SearchResponse<Object> response =
                    elasticsearchClient.search(searchRequest, Object.class);

            List<Hit<Object>> hits = response.hits().hits();
            if (!hits.isEmpty()) {
                return hits.get(0).score(); // 가장 유사한 점수 = 코사인 유사도값
            }
        } catch (Exception e) {
            throw new RuntimeException("Elasticsearch 유사도 연산 실패", e);
        }
        return 0.0;
    }
}
