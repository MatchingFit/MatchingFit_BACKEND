package com.example.matching_fit.domain.score.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.example.matching_fit.domain.resume.entity.Resume;
import com.example.matching_fit.domain.resume.repository.ResumeRepository;
import com.example.matching_fit.domain.score.dto.KeywordScoreDTO;
import com.example.matching_fit.domain.score.entity.Competency;
import com.example.matching_fit.domain.score.entity.CompetencyScore;
import com.example.matching_fit.domain.score.entity.Keyword;
import com.example.matching_fit.domain.score.entity.KeywordScore;
import com.example.matching_fit.domain.score.repository.CompetencyRepository;
import com.example.matching_fit.domain.score.repository.CompetencyScoreRepository;
import com.example.matching_fit.domain.score.repository.KeywordRepository;
import com.example.matching_fit.domain.score.repository.KeywordScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ElasticsearchService {
    private final ElasticsearchClient elasticsearchClient;
    private final ResumeRepository resumeRepository;
    private final KeywordRepository keywordRepository;
    private final KeywordScoreRepository keywordScoreRepository;
    private final CompetencyScoreRepository competencyScoreRepository;
    private final CompetencyRepository competencyRepository;

    public List<KeywordScoreDTO> getAllCosineScoreDTOs(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(()-> new IllegalArgumentException("이력서 없음"));
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
                        .params(params)
                )
        );

        //엘라스틱 서치 최대한번만 돌리기(만번)
        int MAX_RESULTS = 10000;
        List<KeywordScore> ksEntities = new ArrayList<>(); // 저장할 엔티티 컬렉션
        List<KeywordScoreDTO> ksdtoList = new ArrayList<>(); // DTO리스트
        Map<String, Double> competencyScoreMap = new HashMap<>(); //역량 점수 저장하는 맵

        try {
            SearchRequest searchRequest = SearchRequest.of(b -> b
                    .index("your_index_name") //사용할 인덱스명 넣기
                    .query(q -> q
                            .scriptScore(ss -> ss
                                    .query(q2 -> q2.matchAll(ma -> ma))
                                    .script(script)
                            )
                    )
                    .size(MAX_RESULTS)
                    .sort(s -> s.score(sc -> sc.order(SortOrder.Desc)))
                    .sort(s -> s.field(f -> f.field("your_id_field").order(SortOrder.Asc))) // 엘라스틱서치 각 문서의 고유식별자 필드명 / 중복방지
            );

            SearchResponse<Object> response =
                    elasticsearchClient.search(searchRequest, Object.class);

            List<Hit<Object>> hits = response.hits().hits();
            if (hits != null && !hits.isEmpty()) {
                for (Hit<Object> hit : hits) {
                    double score = (hit.score() != null ? hit.score() - 1.0 : 0.0);

                    // hit.source로 keywordId(혹은 해당 고유값) 추출
                    String keywordId = null;
                    if (hit.source() instanceof Map<?, ?> src) {
                        Object idObj = src.get("your_id_field"); // 엘라스틱서치 각 문서의 고유식별자 필드명
                        if (idObj != null) {
                            keywordId = idObj.toString();
                        }
                    }

                    if (keywordId != null) {
                        Keyword keyword = keywordRepository.findById(Long.parseLong(keywordId)).orElse(null);

                        if (keyword != null) {
                            String competencyName = keyword.getCompetency().getName(); //키워드를 찾아와서 해당키워드의 역량을 가져와서
                            double prev = competencyScoreMap.getOrDefault(competencyName, 0.0); //이전점수를 더해서 역량점수를 합침, 잇으면 점수더하고 없으면 0점으로 추가
                            competencyScoreMap.put(competencyName, prev + score); //역량, 점수를 넣음

                            KeywordScore keywordScore = KeywordScore.builder()
                                    .resume(resume)
                                    .competency(keyword.getCompetency()) // 역량정보도 연결
                                    .keyword(keyword)
                                    .score(score)
                                    .build();
                            ksEntities.add(keywordScore);

                            ksdtoList.add(KeywordScoreDTO.builder()
                                    .keywordName(keyword.getKeyword())
                                    .score(score)
                                    .category(keyword.getCategory())
                                    .userId(resume.getUser().getId())
                                    .userName(resume.getUser().getName())
                                    .build()
                            );
                        }
                    }
                }
                // DB 저장(한 번에 일괄 저장)
                if (!ksEntities.isEmpty()) {
                    keywordScoreRepository.saveAll(ksEntities);
                }
                //역량별 점수 DB저장
                List<CompetencyScore> csEntities = new ArrayList<>();
                for (Map.Entry<String, Double> entry : competencyScoreMap.entrySet()){
                    String competencyName = entry.getKey();
                    Double totalScore = entry.getValue();

                    //역량 엔티티 조회
                    Competency competency = competencyRepository.findByName(competencyName)
                            .orElseThrow(()-> new IllegalArgumentException("역량 없음" + competencyName));

                    csEntities.add(CompetencyScore.builder()
                            .resume(resume)
                            .competency(competency)
                            .totalScore(totalScore)
                            .build()
                    );
                    // DB 저장(한 번에 일괄 저장)
                    if (!csEntities.isEmpty()) {
                        competencyScoreRepository.saveAll(csEntities);
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Elasticsearch 유사도 연산/점수 저장 실패", e);
        }
        return ksdtoList;
    }
}