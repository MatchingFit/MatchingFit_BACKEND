package com.example.matching_fit.global.initalizer;

import com.example.matching_fit.global.initalizer.service.KeywordInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeywordInitializer {

    private final KeywordInitService keywordInitService;
    private final WebClient webClient;

    @Value("${health.url.analyzer}")
    private String ANALYZER_HEALTH_URL;

    @Value("${health.url.elastic}")
    private String ELASTIC_HEALTH_URL;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        waitForExternalService("Analyzer", ANALYZER_HEALTH_URL);
        waitForExternalService("Elasticsearch", ELASTIC_HEALTH_URL);

        log.info("[✔️] 외부 서비스 모두 준비 완료 → 키워드 초기화 시작");
        keywordInitService.initializeAllKeywords();
        log.info("[✔️] 키워드 초기화 완료");
    }

    private void waitForExternalService(String name, String url) {
        int retries = 10;
        int delayMillis = 5000;

        for (int i = 0; i < retries; i++) {
            try {
                webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(String.class)
                        .timeout(java.time.Duration.ofSeconds(3))
                        .block();  // 동기 대기

                log.info("[{}] {} 준비됨", name, url);
                return;
            } catch (Exception e) {
                log.warn("[{}] 아직 준비되지 않음: {} (시도 {}/{})", name, url, i + 1, retries);
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        throw new IllegalStateException(name + " 서비스가 준비되지 않았습니다: " + url);
    }
}
