package com.example.matching_fit.domain.score.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.example.matching_fit.domain.resume.entity.Resume;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ElasticsearchService {
    private final ElasticsearchClient elasticsearchClient;

    public List<Double> getAllCosineScores(Resume resume) {
        Double[] resumeArr = resume.getEmbedding();
        if (resumeArr == null) return Collections.emptyList();
        List<Float> resumeFloatList = Arrays.stream(resumeArr)
                .map(Double::floatValue)
                .toList();

        Map<String, JsonData> params = new HashMap<>();
        params.put("query_vector", JsonData.of(resumeFloatList));

        Script script = Script.of(s -> s
                .inline(i -> i
                        .source("cosineSimilarity(params.query_vector, 'embedding') + 1.0")
                        .lang("painless")
                        .params(params)
                )
        );

        int PAGE_SIZE = 10000;
        List<Double> allScores = new ArrayList<>();
        List<FieldValue> searchAfter = null;

        try {
            while (true) {
                SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                        .index("your_index_name")
                        .query(q -> q
                                .scriptScore(ss -> ss
                                        .query(q2 -> q2.matchAll(ma -> ma))
                                        .script(script)
                                )
                        )
                        .size(PAGE_SIZE)
                        .sort(s -> s.score(sc -> sc.order(SortOrder.Desc)))
                        .sort(s -> s.field(f -> f.field("your_id_field").order(SortOrder.Asc))); // 유니크 필드 필수

                if (searchAfter != null) {
                    searchBuilder.searchAfter(searchAfter);
                }

                SearchResponse<Object> response =
                        elasticsearchClient.search(searchBuilder.build(), Object.class);

                List<Hit<Object>> hits = response.hits().hits();
                if (hits == null || hits.isEmpty()) break;

                allScores.addAll(
                        hits.stream()
                                .map(hit -> (hit.score() != null ? hit.score() - 1.0 : 0.0))
                                .toList()
                );

                // -> 마지막 hit의 _score, id 값 직접 추출
                Hit<Object> lastHit = hits.get(hits.size() - 1);
                Double lastScore = lastHit.score();

                // 직접 source에서 id필드 파싱 (id필드 = ES매핑시 저장한 고유키명)
                String lastId = null;
                if (lastHit.source() instanceof Map) {
                    Map<String, Object> src = (Map<String, Object>) lastHit.source();
                    lastId = src.get("your_id_field").toString();
                }
                if (lastScore == null || lastId == null) break; // 더 이상 페이지가 없음

                searchAfter = List.of(FieldValue.of(lastScore), FieldValue.of(lastId));
            }
        } catch (Exception e) {
            throw new RuntimeException("Elasticsearch search_after 유사도 연산 실패", e);
        }
        return allScores;
    }
}