package com.example.matching_fit.domain.score.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.example.matching_fit.domain.resume.entity.Resume;
import com.example.matching_fit.domain.resume.repository.ResumeRepository;
import com.example.matching_fit.domain.score.dto.KeywordScoreDTO;
import com.example.matching_fit.domain.score.dto.CompetencyScoreDTO;
import com.example.matching_fit.domain.score.entity.*;
import com.example.matching_fit.domain.score.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchService {
    private final ElasticsearchClient elasticsearchClient;
    private final ResumeRepository resumeRepository;
    private final KeywordRepository keywordRepository;
    private final KeywordScoreRepository keywordScoreRepository;
    private final CompetencyScoreRepository competencyScoreRepository;
    private final CompetencyRepository competencyRepository;

    @Transactional
    public List<CompetencyScoreDTO> getAllCosineScoreDTOs(Long resumeId, List<Double> resumeEmbedding) {
        log.info("🔍 [START] 이력서 점수 계산 시작: resumeId = {}", resumeId);

        Optional<Resume> optionalResume = resumeRepository.findById(resumeId);

        Resume resume = optionalResume.orElseThrow(() -> new IllegalArgumentException("이력서 없음"));

        if (resumeEmbedding == null || resumeEmbedding.isEmpty()) {
            log.warn("❗ 임베딩 데이터 없음: resumeId = {}", resumeId);
            return Collections.emptyList();
        }

        Map<String, JsonData> params = new HashMap<>();
        params.put("query_vector", JsonData.of(resumeEmbedding));

        Script script = Script.of(s -> s
                .inline(i -> i
                        .source("cosineSimilarity(params.query_vector, 'embedding') + 1.0")
                        .params(params)
                )
        );

        int MAX_RESULTS = 10000;
        List<KeywordScore> ksEntities = new ArrayList<>();
        Map<String, List<KeywordScoreDTO>> competencyKeywordMap = new HashMap<>();
        Map<String, Double> competencyScoreMap = new HashMap<>();

        try {
            log.info("📡 Elasticsearch 스크립트 쿼리 실행 준비...");

            SearchRequest searchRequest = SearchRequest.of(b -> b
                    .index("keywords")
                    .query(q -> q
                            .scriptScore(ss -> ss
                                    .query(q2 -> q2.matchAll(ma -> ma))
                                    .script(script)
                            )
                    )
                    .size(MAX_RESULTS)
                    .sort(s -> s.score(sc -> sc.order(SortOrder.Desc)))
            );

            SearchResponse<Object> response = elasticsearchClient.search(searchRequest, Object.class);
            List<Hit<Object>> hits = response.hits().hits();

            log.info("✅ Elasticsearch 검색 결과 수: {}", hits.size());

            for (Hit<Object> hit : hits) {
                double score = (hit.score() != null ? hit.score() - 1.0 : 0.0);
                String keywordId = hit.id();

                try {
                    Long id = Long.parseLong(keywordId);
                    keywordRepository.findByIdWithCompetency(id).ifPresent(keyword -> {
                        log.debug("➡️ 유효한 키워드 ID: {}, 키워드명: {}", id, keyword.getKeyword());

                        String competencyName = keyword.getCompetency().getName();
                        double prev = competencyScoreMap.getOrDefault(competencyName, 0.0);
                        competencyScoreMap.put(competencyName, prev + score);

                        ksEntities.add(KeywordScore.builder()
                                .resume(resume)
                                .competency(keyword.getCompetency())
                                .keyword(keyword)
                                .score(score)
                                .build());

                        KeywordScoreDTO keywordScoreDTO = KeywordScoreDTO.builder()
                                .keywordName(keyword.getKeyword())
                                .score(score)
                                .category(keyword.getCategory())
                                .build();

                        // 역량별 키워드 점수 맵에 추가
                        competencyKeywordMap.computeIfAbsent(competencyName, k -> new ArrayList<>()).add(keywordScoreDTO);
                    });
                } catch (NumberFormatException e) {
                    log.warn("⚠️ keywordId '{}'는 숫자가 아닙니다. 무시합니다.", keywordId);
                }
            }

            if (!ksEntities.isEmpty()) {
                log.info("💾 키워드 점수 저장 개수: {}", ksEntities.size());

                keywordScoreRepository.saveAll(ksEntities);
            }

            List<CompetencyScore> csEntities = new ArrayList<>();
            for (Map.Entry<String, Double> entry : competencyScoreMap.entrySet()) {
                String competencyName = entry.getKey();
                Double totalScore = entry.getValue();

                Competency competency = competencyRepository.findByName(competencyName)
                        .orElseThrow(() -> new IllegalArgumentException("역량 없음: " + competencyName));

                log.debug("🧠 역량: {}, 총점: {}", competencyName, totalScore);

                csEntities.add(CompetencyScore.builder()
                        .resume(resume)
                        .competency(competency)
                        .totalScore(totalScore)
                        .build());
            }

            if (!csEntities.isEmpty()) {
                log.info("💾 역량 점수 저장 개수: {}", csEntities.size());

                competencyScoreRepository.saveAll(csEntities);
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Elasticsearch 유사도 연산/점수 저장 실패", e);
        }

        // CompetencyScoreDTO 리스트 생성
        List<CompetencyScoreDTO> competencyScoreDTOs = new ArrayList<>();
        for (Map.Entry<String, Double> entry : competencyScoreMap.entrySet()) {
            String competencyName = entry.getKey();
            Double totalScore = entry.getValue();
            List<KeywordScoreDTO> keywordScores = competencyKeywordMap.getOrDefault(competencyName, new ArrayList<>());

            competencyScoreDTOs.add(CompetencyScoreDTO.builder()
                    .competencyName(competencyName)
                    .totalScore(totalScore)
                    .keywordScoreDTOS(keywordScores)
                    .build());
        }

        log.info("✅ [DONE] 이력서 점수 계산 완료: resumeId = {}", resumeId);
        return competencyScoreDTOs;
    }
}
