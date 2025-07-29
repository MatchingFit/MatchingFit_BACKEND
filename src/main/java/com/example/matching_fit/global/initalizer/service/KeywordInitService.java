package com.example.matching_fit.global.initalizer.service;

import com.example.matching_fit.domain.score.entity.Category;
import com.example.matching_fit.domain.score.entity.Competency;
import com.example.matching_fit.domain.score.entity.Keyword;
import com.example.matching_fit.domain.score.repository.CompetencyRepository;
import com.example.matching_fit.domain.score.repository.KeywordRepository;
import com.example.matching_fit.global.initalizer.dto.KeywordYamlDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeywordInitService {

    private final CompetencyRepository competencyRepository;
    private final KeywordRepository keywordRepository;
    private final WebClient webClient;
    private final KeywordYamlDto keywordYamlDto;

    @Value("${embedding.api.url}")
    private String embeddingApiUrl;

    @Transactional
    public void initializeAllKeywords() {
        // 1. 기존 키워드 개수 확인 (삭제하지 않음)
        List<Keyword> existingKeywords = keywordRepository.findAll();
        log.info("📊 기존 키워드 {}개 유지", existingKeywords.size());

        // 2. 역량 + 역량별 키워드 등록 (중복 체크)
        initializeCompetenciesAndKeywords();

        // 3. 기술 키워드 등록 (Category 기반, 중복 체크)
        initializeTechnicalKeywords();

        // 4. 벡터 임베딩 서버 호출
        callEmbeddingAPI();

        log.info("✅ 전체 키워드 초기화 완료");
    }


    @Transactional
    public void initializeCompetenciesAndKeywords() {
        log.info("▶ 역량 및 키워드 초기화를 시작합니다.");

        Map<String, Map<String, Double>> competencyKeywords = keywordYamlDto.getCompetencyKeywords();

        for (Map.Entry<String, Map<String, Double>> entry : competencyKeywords.entrySet()) {
            String competencyName = entry.getKey();
            Map<String, Double> keywordMap = entry.getValue();

            Competency competency = competencyRepository.findByName(competencyName)
                    .orElseGet(() -> {
                        Competency newComp = Competency.builder()
                                .name(competencyName)
                                .build();
                        competencyRepository.save(newComp);
                        log.info("✔ 역량 '{}' 삽입", competencyName);
                        return newComp;
                    });

            for (Map.Entry<String, Double> keywordEntry : keywordMap.entrySet()) {
                String keyword = keywordEntry.getKey();
                Double weight = keywordEntry.getValue();

                // 중복 체크 - 해당 역량의 키워드 목록에서 확인
                List<Keyword> existingKeywords = keywordRepository.findByCompetency(competency);
                boolean exists = existingKeywords.stream()
                        .anyMatch(k -> k.getKeyword().equals(keyword));
                
                if (!exists) {
                    keywordRepository.save(Keyword.builder()
                            .keyword(keyword)
                            .competency(competency)
                            .weightScore(weight)
                            .build());
                    log.info("✔ [{}] 역량 키워드 '{}' 삽입 (가중치: {})", competencyName, keyword, weight);
                } else {
                    log.info("⏭️ [{}] 역량 키워드 '{}' 이미 존재 (건너뜀)", competencyName, keyword);
                }
            }
        }

        log.info("✅ 역량 및 키워드 초기화 완료");
    }

    @Transactional
    public void initializeTechnicalKeywords() {
        log.info("▶ 기술 키워드 초기화를 시작합니다.");

        // "기술 전문성" 역량 가져오기 또는 생성
        Competency technicalCompetency = competencyRepository.findByName("기술 전문성")
                .orElseGet(() -> {
                    Competency newComp = Competency.builder()
                            .name("기술 전문성")
                            .build();
                    competencyRepository.save(newComp);
                    log.info("✔ 역량 '기술 전문성' 삽입");
                    return newComp;
                });

        Map<Category, Map<String, Double>> techKeywords = keywordYamlDto.getTechnicalKeywordsAsEnumMap();

        for (Map.Entry<Category, Map<String, Double>> entry : techKeywords.entrySet()) {
            Category category = entry.getKey();
            Map<String, Double> keywordMap = entry.getValue();

            for (Map.Entry<String, Double> keywordEntry : keywordMap.entrySet()) {
                String keyword = keywordEntry.getKey();
                Double weight = keywordEntry.getValue();

                // 중복 체크 - 해당 카테고리의 키워드 목록에서 확인
                List<Keyword> existingKeywords = keywordRepository.findByCategory(category);
                boolean exists = existingKeywords.stream()
                        .anyMatch(k -> k.getKeyword().equals(keyword));
                
                if (!exists) {
                    keywordRepository.save(Keyword.builder()
                            .keyword(keyword)
                            .category(category)
                            .competency(technicalCompetency)
                            .weightScore(weight)
                            .build());
                    log.info("✔ [{}] 기술 키워드 '{}' 삽입 (가중치: {})", category.getLabel(), keyword, weight);
                } else {
                    log.info("⏭️ [{}] 기술 키워드 '{}' 이미 존재 (건너뜀)", category.getLabel(), keyword);
                }
            }
        }

        log.info("✅ 기술 키워드 초기화 완료");
    }

    public void callEmbeddingAPI() {
        log.info("▶ FastAPI에 임베딩 요청을 보냅니다.");

        try {
            String response = webClient.post()
                    .uri(embeddingApiUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("✅ 임베딩 결과: {}", response);
        } catch (Exception e) {
            log.error("❌ 임베딩 API 호출 실패", e);
        }
    }
}