package com.example.matching_fit.domain.score.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.example.matching_fit.domain.score.entity.ResumeEmbedding;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ElasticsearchService {
    private final ElasticsearchClient elasticsearchClient;

    public double getCosineScore(ResumeEmbedding resume) {
        List<Double> resumeVec = resume.getEmbeddingVectorAsList();
        List<Float> resumeFloatList = resumeVec.stream()
                .map(Double::floatValue)
                .collect(Collectors.toList());

        Map<String, JsonData> params = new HashMap<>();
        params.put("query_vector", JsonData.of(resumeFloatList));

        try {
            Script script = Script.of(s -> s
                    .inline(i -> i
                            .source("cosineSimilarity(params.query_vector, 'embedding') + 1.0")
                            .lang("painless")
                            .params(params)
                    )
            );

            SearchRequest searchRequest = SearchRequest.of(b -> b
                    .index("index") //엘라스틱서치에 데이터가 저장된 인덱스명으로 변경
                    .query(q -> q
                            .scriptScore(ss -> ss
                                    .query(q2 -> q2.matchAll(ma -> ma))
                                    .script(script)
                            )
                    )
                    .size(1)
            );

            SearchResponse<Object> response =
                    elasticsearchClient.search(searchRequest, Object.class);

            List<Hit<Object>> hits = response.hits().hits();
            if (!hits.isEmpty()) {
                return hits.get(0).score() - 1.0; // +1.0 보정한 것 복구
            }
        } catch (Exception e) {
            throw new RuntimeException("Elasticsearch 유사도 연산 실패", e);
        }
        return 0.0; //결과가 없으면 0점
    }
}