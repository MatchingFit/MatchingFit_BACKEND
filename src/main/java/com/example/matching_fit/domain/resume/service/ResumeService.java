package com.example.matching_fit.domain.resume.service;

import com.example.matching_fit.domain.gpt.service.OpenAiService;
import com.example.matching_fit.domain.resume.dto.ResumeAnalysisResultDto;
import com.example.matching_fit.domain.resume.dto.ResumeTextDto;
import com.example.matching_fit.domain.resume.entity.Resume;
import com.example.matching_fit.domain.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final OpenAiService openAiService;
    private final String openAiApiKey;
    private final RestTemplate restTemplate;

    public ResumeService(ResumeRepository resumeRepository,
                         OpenAiService openAiService,
                         @Value("${openai.api.key}") String openAiApiKey) {
        this.resumeRepository = resumeRepository;
        this.openAiService = openAiService;
        this.openAiApiKey = openAiApiKey;
        this.restTemplate = new RestTemplate();
    }

    public ResumeAnalysisResultDto analyzeResumeById(Long id) throws Exception {
        long startTotal = System.currentTimeMillis();

        long startFetch = System.currentTimeMillis();
        ResumeTextDto resumeDto = resumeRepository.findResumeTextDtoById(id)
                .orElseThrow(() -> new NoSuchElementException("이력서가 존재하지 않습니다. id=" + id));

        String textS3Url = resumeDto.getTextS3Url();
        if (textS3Url == null || textS3Url.isEmpty()) {
            throw new IllegalArgumentException("이력서에 텍스트 S3 URL이 없습니다. id=" + id);
        }

        String text = fetchTextFromS3(textS3Url);
        long endFetch = System.currentTimeMillis();
        log.info("[⏱️ 이력서 텍스트 추출 시간] " + (endFetch - startFetch) + "ms");

        // 이력서를 쪼개서 분석
        List<String> chunks = splitTextIntoChunks(text, 1500);

        long startAnalyze = System.currentTimeMillis();
        ResumeAnalysisResultDto result = analyzeTextChunksWithOpenAI(chunks);
        long endAnalyze = System.currentTimeMillis();
        log.info("[⏱️ GPT 분석 + 요약 시간] " + (endAnalyze - startAnalyze) + "ms");

        long endTotal = System.currentTimeMillis();
        log.info("[✅ 전체 수행 시간] " + (endTotal - startTotal) + "ms");

        return result;
    }


    private String fetchTextFromS3(String s3Url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            headers.set("Accept", "*/*");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(s3Url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("S3 요청 실패: HTTP " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            throw new RuntimeException("S3 요청 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 이력서를 문단 또는 줄 단위로 나눠서 일정 길이 이하로 분할
     */
    private List<String> splitTextIntoChunks(String text, int maxChunkLength) {
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        for (String paragraph : text.split("\n")) {
            if (currentChunk.length() + paragraph.length() > maxChunkLength) {
                chunks.add(currentChunk.toString());
                currentChunk = new StringBuilder();
            }
            currentChunk.append(paragraph).append("\n");
        }
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }
        return chunks;
    }

    /**
     * 분할된 이력서 조각을 순차적으로 분석하고 이어붙임
     */
    private ResumeAnalysisResultDto analyzeTextChunksWithOpenAI(List<String> chunks) {
        List<ResumeAnalysisResultDto.ChunkAnalysis> chunkAnalyses = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            String prompt = (i == 0)
                    ? "다음 이력서를 한국어로 분석해줘:\n" + chunks.get(i)
                    : "이전 분석 내용을 기반으로 다음 이력서 내용을 한국어로 계속 분석해줘:\n" + chunks.get(i);

            String analysis = sendChatCompletion(prompt);

            // 🔽 분석 결과 요약
            String summaryPrompt = "다음은 이력서의 일부에 대한 분석 결과입니다. 이 내용을 500자 이내로 간단히 요약해줘:\n" + analysis;
            String summary = sendChatCompletion(summaryPrompt);

            ResumeAnalysisResultDto.ChunkAnalysis chunkAnalysis = new ResumeAnalysisResultDto.ChunkAnalysis();
            chunkAnalysis.setPartNumber(i + 1);
            chunkAnalysis.setOriginalAnalysis(analysis);
            chunkAnalysis.setSummary(summary);
            chunkAnalyses.add(chunkAnalysis);
        }

        // ✅ 최종 요약
        StringBuilder combinedSummaries = new StringBuilder();
        for (ResumeAnalysisResultDto.ChunkAnalysis c : chunkAnalyses) {
            combinedSummaries.append("[").append(c.getPartNumber()).append("부 요약] ")
                    .append(c.getSummary()).append("\n");
        }

        String finalSummaryPrompt =
                "다음은 이력서를 여러 부분으로 나누어 분석한 후 각각 요약한 내용입니다.\n\n" +
                        "이 내용들을 기반으로 아래 형식과 기준에 따라 **자세하고 구체적으로** 이력서를 한국어로 분석해줘. \n" +
                        "각 항목은 반드시 항목 제목을 포함하고, **문장 형태로 풍부하게 설명**해줘.\n\n" +
                        "📌 분석 기준:\n" +
                        "1. 핵심 강점 (3가지 이상, 문장으로 설명)\n" +
                        "2. 보완할 점 또는 약점 (2~3가지, 근거 포함)\n" +
                        "3. 기술 스택 요약 (사용한 기술과 숙련도)\n" +
                        "4. 추천 직무 또는 포지션 (이력서 기반으로 구체적인 직무 예시 제시)\n\n" +
                        "✍️ 요약된 분석들:\n" + combinedSummaries;

        String finalSummary = sendChatCompletion(finalSummaryPrompt);

        ResumeAnalysisResultDto resultDto = new ResumeAnalysisResultDto();
        resultDto.setChunkAnalyses(chunkAnalyses);
        resultDto.setFinalSummary(finalSummary);

        return resultDto;
    }

    /**
     * OpenAI Chat Completion 요청
     */
    private String sendChatCompletion(String userContent) {
        String apiUrl = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo",  // 가능한 모델 이름으로 변경
                "messages", List.of(
                        Map.of("role", "system", "content", "당신은 이력서 분석 도우미입니다."),
                        Map.of("role", "user", "content", userContent)
                ),
                "max_tokens", 1000,
                "temperature", 0.2
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                var choices = (List<Map<String, Object>>) body.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null) {
                        return (String) message.get("content");
                    }
                }
                return "분석 결과를 받지 못했습니다.";
            } else {
                return "OpenAI API 호출 실패: 상태 코드 " + response.getStatusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "분석 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
}
