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
import com.example.matching_fit.domain.score.dto.ScoreRequestDTO;
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
    public List<CompetencyScoreDTO> getAllCosineScoreDTOs(ScoreRequestDTO scoreRequestDTO) {
        Long resumeId = scoreRequestDTO.getResumeId();
        List<Double> resumeEmbedding = scoreRequestDTO.getEmbedding();
        String jobField = scoreRequestDTO.getJobField();

        //입력된 jobField 값 로그 찍기 (입력값 확인)
        log.info("▶▶▶ 입력된 jobField(선택 카테고리): '{}'", jobField);

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
        //추가
        //jobField를 소문자로 변환해 일관성 있는 비교(일관성있는 처리를 위해)
        String choiceCategory = (jobField != null) ? jobField.trim().replaceAll("\\s+", "").toLowerCase() : null;

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
                        //추가
                        String categoryLabel = (keyword.getCategory() != null)
                                ? keyword.getCategory().getLabel().trim().replaceAll("\\s+", "").toLowerCase()
                                : null;
                        log.info(">>> 비교 로그 위치 도달: keywordId={}, resumeId={}", id, resumeId);
                        log.debug(">>> 비교 중: 선택한 카테고리='{}', 키워드 카테고리='{}', 역량 이름='{}'",
                                choiceCategory, categoryLabel, competencyName);
                        log.debug(">> 필터 조건 검사: 역량='{}', 입력카테고리='{}', 키워드카테고리='{}'",
                                competencyName, choiceCategory, categoryLabel);

                        if ("기술 전문성".equals(competencyName)
                                && choiceCategory != null
                                && !choiceCategory.isEmpty()) {
                            if (choiceCategory.equals(categoryLabel)) {
                                log.debug(">> 점수 누적 중: 역량='{}', 키워드='{}', 점수={}",
                                        competencyName, keyword.getKeyword(), score);
                                accumulateKeywordScore(score, resume, keyword, competencyName,
                                        competencyScoreMap, ksEntities, competencyKeywordMap);
                            } else {
                                log.debug("❌ 카테고리 필터 불일치: 선택카테고리='{}', 키워드카테고리='{}', 역량='{}'",
                                        choiceCategory, categoryLabel, competencyName);
                            }
                        } else {
                            log.debug(">> 기술전문성 외 역량은 필터 없이 점수 누적: 역량='{}', 키워드='{}', 점수={}", competencyName, keyword.getKeyword(), score);
                            // 기술전문성 외 다른 역량은 모두 점수 누적
                            accumulateKeywordScore(score, resume, keyword, competencyName,
                                    competencyScoreMap, ksEntities, competencyKeywordMap);
                        }
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
    // 중복 코드 제거를 위한 점수 누적 및 DTO 처리 메서드
    private void accumulateKeywordScore(double score, Resume resume, Keyword keyword, String competencyName,
                                        Map<String, Double> competencyScoreMap, List<KeywordScore> ksEntities,
                                        Map<String, List<KeywordScoreDTO>> competencyKeywordMap) {
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
                .category(categoryLabel(keyword))
                .build();

        competencyKeywordMap.computeIfAbsent(competencyName, k -> new ArrayList<>()).add(keywordScoreDTO);
    }

    private String categoryLabel(Keyword keyword) {
        return (keyword.getCategory() != null)
                ? keyword.getCategory().getLabel()
                : null;
    }
}
