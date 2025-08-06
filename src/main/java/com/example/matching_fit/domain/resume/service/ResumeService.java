package com.example.matching_fit.domain.resume.service;

import com.example.matching_fit.domain.manager.resume_matching_result.repository.ResumeMatchingResultRepository;
import com.example.matching_fit.domain.resume.dto.ResumeAnalysisResultDto;
import com.example.matching_fit.domain.resume.dto.ResumeSummaryDto;
import com.example.matching_fit.domain.resume.dto.ResumeTextDto;
import com.example.matching_fit.domain.resume.entity.Resume;
import com.example.matching_fit.domain.resume.repository.ResumeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ResumeService {
    private final ResumeRepository resumeRepository;
    private final String openAiApiKey;
    private final RestTemplate restTemplate;

    public ResumeService(ResumeRepository resumeRepository,
                         @Value("${openai.api.key}") String openAiApiKey) {
        this.resumeRepository = resumeRepository;
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

        List<String> chunks = splitTextIntoChunks(text, 3000);

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

    private ResumeAnalysisResultDto analyzeTextChunksWithOpenAI(List<String> chunks) {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<CompletableFuture<ResumeAnalysisResultDto.ChunkAnalysis>> futures = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            final int partNumber = i + 1;
            final String chunk = chunks.get(i);
            futures.add(CompletableFuture.supplyAsync(() -> {
                String prompt = "다음 이력서 내용을 분석하고, 1000자 이내로 요약까지 함께 작성해줘.\n" +
                        "분석 결과를 먼저 쓰고, 마지막에 [요약:] 뒤에 요약을 적어줘:\n\n" + chunk;
                String response = sendChatCompletion(prompt);
                String[] split = response.split("요약[:：]?", 2);

                ResumeAnalysisResultDto.ChunkAnalysis chunkAnalysis = new ResumeAnalysisResultDto.ChunkAnalysis();
                chunkAnalysis.setPartNumber(partNumber);
                chunkAnalysis.setOriginalAnalysis(split[0].trim());
                chunkAnalysis.setSummary(split.length > 1 ? split[1].trim() : "");
                return chunkAnalysis;
            }, executor));
        }

        List<ResumeAnalysisResultDto.ChunkAnalysis> chunkAnalyses = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        StringBuilder combinedSummaries = new StringBuilder();
        for (ResumeAnalysisResultDto.ChunkAnalysis c : chunkAnalyses) {
            combinedSummaries.append("[").append(c.getPartNumber()).append("부 요약] ")
                    .append(c.getSummary()).append("\n");
        }

        String finalSummaryPrompt =
                "다음은 이력서를 여러 부분으로 나누어 분석한 후 각각 요약한 내용입니다.\n\n" +
                        "이 요약들을 바탕으로 이력서를 아래의 항목 형식에 맞추어 한국어로 분석해줘. \n" +
                        "각 항목은 반드시 제목으로 시작하고, 제목은 아래와 정확히 일치해야 하며, 항상 이 순서로 작성해줘:\n\n" +
                        "- 각 항목은 마크다운 형식이 아닌 '1. 핵심 강점'처럼 숫자와 점(.)으로 시작하고, 반드시 줄의 맨 앞에 위치해야 함.\n" +
                        "- 각 제목은 반드시 정확히 '1. 핵심 강점', '2. 보완할 점 또는 약점', '3. 기술 스택 요약', '4. 추천 직무 또는 포지션' 중 하나여야 함.\n" +

                        "1. 핵심 강점\n" +
                        "2. 보완할 점 또는 약점\n" +
                        "3. 기술 스택 요약\n" +
                        "4. 추천 직무 또는 포지션\n\n" +

                        "✍️ 작성 규칙:\n" +
                        "- 각 항목 아래에 문단 단위로 서술하되, 문장 형태로 자세히 작성할 것.\n" +
                        "- 각 항목마다 여러 문장으로 자세히 작성하고, 번호나 불릿 없이 작성할 것.\n" +
                        "- 항목 제목은 반드시 위 형식과 정확히 일치하고, 줄바꿈하여 시작할 것.\n" +
                        "- 특수문자 (예: *, •, #, :)는 항목 제목이나 본문에서 사용하지 말 것.\n" +
                        "- 전체는 마치 사람 이력서를 평가 보고서처럼 자연스럽게 이어지게 작성할 것.\n\n" +
                        "- 항목마다 '하위 항목'이나 불릿 포인트(-, *, 1.) 형식으로 작성하지 말고, 자연스러운 단락(문단) 형식으로, 한 문장으로 풀어서 설명할 것. "+
                        "📌 참고 요약들:\n" + combinedSummaries;

        String finalSummaryRaw = sendChatCompletion(finalSummaryPrompt);
        String processedFinalSummary = convertMarkdownToNumbered(finalSummaryRaw);

        ResumeAnalysisResultDto resultDto = new ResumeAnalysisResultDto();
        resultDto.setChunkAnalyses(chunkAnalyses);
        resultDto.setFinalSummary(processedFinalSummary);

//        List<String> summarizedSections = new ArrayList<>();
//        summarizedSections.addAll(extractSection(processedFinalSummary, "1\\. 핵심 강점", "2\\. 보완할 점 또는 약점"));
//        summarizedSections.addAll(extractSection(processedFinalSummary, "2\\. 보완할 점 또는 약점", "3\\. 기술 스택 요약"));
//        summarizedSections.addAll(extractSection(processedFinalSummary, "3\\. 기술 스택 요약", "4\\. 추천 직무 또는 포지션"));
//        summarizedSections.addAll(extractSection(processedFinalSummary, "4\\. 추천 직무 또는 포지션", null));
//        resultDto.setSummarizedSections(summarizedSections);

        // ✅ 요약 섹션 4개 추출
        List<String> summarizedSections = extractFourMainSections(processedFinalSummary);
        resultDto.setSummarizedSections(summarizedSections);

        return resultDto;
    }

    private String convertMarkdownToNumbered(String finalSummary) {
        Map<String, String> headerMap = Map.of(
                "## 핵심 강점", "1. 핵심 강점",
                "## 보완할 점 또는 약점", "2. 보완할 점 또는 약점",
                "## 기술 스택 요약", "3. 기술 스택 요약",
                "## 추천 직무 또는 포지션", "4. 추천 직무 또는 포지션"
        );

        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            finalSummary = finalSummary.replace(entry.getKey(), entry.getValue());
        }
        return finalSummary;
    }

    private List<String> extractSection(String text, String startPattern, String endPattern) {
        String section = "";
        try {
            String regex = "(?m)^" + startPattern + "\\s*\\n+([\\s\\S]*?)" + (endPattern != null ? "(?=^" + endPattern + "\\s*)" : "$");
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                section = matcher.group(1).trim();
            }
        } catch (Exception e) {
            log.warn("분석 섹션 추출 실패: {}", startPattern, e);
        }

        return Arrays.stream(section.split("[\\n\\r]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private String sendChatCompletion(String userContent) {
        String apiUrl = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo-1106",
                "messages", List.of(
                        Map.of("role", "system", "content", "당신은 이력서 분석 도우미입니다."),
                        Map.of("role", "user", "content", userContent)
                ),
                "max_tokens", 1200,
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

    @Transactional
    public void updatePdfUrl(Long resumeId, String pdfUrl) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 resumeId: " + resumeId));

        resume.updatePdfUrl(pdfUrl);
    }

    private List<String> extractFourMainSections(String finalSummary) {
        List<String> sections = new ArrayList<>();
        Pattern pattern = Pattern.compile(
                "1\\. 핵심 강점\\s*\\n(.*?)\\n2\\. 보완할 점 또는 약점\\s*\\n(.*?)\\n3\\. 기술 스택 요약\\s*\\n(.*?)\\n4\\. 추천 직무 또는 포지션\\s*\\n(.*)",
                Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(finalSummary);

        if (matcher.find()) {
            for (int i = 1; i <= 4; i++) {
                String section = matcher.group(i).trim();
                // 불릿(-) 제거 및 문장 정리 (선택적)
                section = section.replaceAll("(?m)^\\s*-\\s*", "").trim();
                sections.add(section);
            }
        }

        return sections;
    }
}


