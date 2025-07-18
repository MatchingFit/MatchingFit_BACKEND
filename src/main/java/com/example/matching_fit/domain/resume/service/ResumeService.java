package com.example.matching_fit.domain.resume.service;

import com.example.matching_fit.domain.gpt.service.OpenAiService;
import com.example.matching_fit.domain.gpt.util.PdfUtil;
import com.example.matching_fit.domain.resume.entity.Resume;
import com.example.matching_fit.domain.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final OpenAiService openAiService;

    public String analyzeResumeById(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 이력서를 찾을 수 없습니다."));

        try (InputStream inputStream = new URL(resume.getFileUrl()).openStream()) {
            // PDF에서 텍스트 추출
            String text = PdfUtil.extractText(inputStream);

            // OpenAI로 분석 요청
            return openAiService.analyzeResume(text);

        } catch (Exception e) {
            throw new RuntimeException("이력서 분석 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
