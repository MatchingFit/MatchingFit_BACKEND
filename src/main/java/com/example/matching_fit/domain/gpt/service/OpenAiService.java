package com.example.matching_fit.domain.gpt.service;

import com.example.matching_fit.global.config.OpenAiConfig;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiService {
    private final OpenAiConfig openAiConfig;
    private final RestTemplate restTemplate;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${openai.api.key}")
    private String apiKey;

    public String getChatCompletion(String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openAiConfig.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", userMessage));
        body.put("messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_URL, request, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

        return message.get("content").toString().trim();
    }

    private final OkHttpClient client = new OkHttpClient();

    public String analyzeResume(String text) throws IOException {
        String prompt = "아래 이력서 내용을 분석해서 주요 기술, 경력, 학력, 강점을 요약해줘:\n\n" + text;

        MediaType mediaType = MediaType.parseMediaType("application/json");
        String json = "{"
                + "\"model\": \"text-davinci-003\","
                + "\"prompt\": " + "\"" + prompt.replace("\"", "\\\"") + "\","
                + "\"max_tokens\": 500,"
                + "\"temperature\": 0.7"
                + "}";

        RequestBody body = RequestBody.create(json, okhttp3.MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/completions")
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("OpenAI API error: " + response);
            }
            String responseBody = response.body().string();
            // JSON에서 "choices[0].text" 부분 파싱 필요 (간단하게 JSON 파싱 라이브러리 사용 권장)
            // 여기서는 임시로 전체 응답 리턴
            return responseBody;
        }
    }
}
