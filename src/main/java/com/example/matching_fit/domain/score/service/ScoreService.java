package com.example.matching_fit.domain.score.service;

import com.example.matching_fit.domain.resume.entity.Resume;
import com.example.matching_fit.domain.resume.repository.ResumeRepository;
import com.example.matching_fit.domain.score.dto.CompetencyScoreDTO;
import com.example.matching_fit.domain.score.dto.KeywordScoreDTO;
import com.example.matching_fit.domain.score.entity.*;
import com.example.matching_fit.domain.score.repository.*;
import com.example.matching_fit.domain.score.util.EmbeddingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ScoreService {
    private final CompetencyRepository competencyRepository;
    private final KeywordRepository keywordRepository;
    private final CompetencyScoreRepository competencyScoreRepository;
    private final KeywordScoreRepository keywordScoreRepository;
    private final ResumeRepository resumeRepository;

    //이력서를 받아서 점수로 반환하기
    @Transactional
    public List<CompetencyScoreDTO> sumScore(Long resumeId) { // [수정]
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("이력서 없음"));
        Double[] resumeVec = resume.getEmbedding();
        Long userId = resume.getUser().getId();

        List<CompetencyScoreDTO> result = new ArrayList<>();
        for (Competency competency : competencyRepository.findAll()) {
            List<Keyword> keywords = keywordRepository.findByCompetency(competency);
            double totalScore = 0.0;
            List<KeywordScoreDTO> keywordDTOs = new ArrayList<>();

            for (Keyword keyword : keywords) {
                Double[] keywordVec = keyword.getEmbedding();
                if (keywordVec == null || resumeVec == null) continue;
                double rawScore = EmbeddingUtil.cosineSimilarity(resumeVec, keywordVec);

                double weight = (keyword.getWeightScore() != null) ? keyword.getWeightScore() : 1.0;
                double weightedScore = rawScore * weight;

                keywordScoreRepository.save(
                        KeywordScore.builder()
                                .userId(userId)
                                .resumeId(resumeId)
                                .competency(competency)
                                .keyword(keyword)
                                .score(weightedScore)
                                .build()
                );
                keywordDTOs.add(KeywordScoreDTO.builder()
                        .keywordName(keyword.getKeyword())
                        .score(weightedScore)
                        .category(keyword.getCategory())
                        .build());
                totalScore += weightedScore;
            }

            double avgScore = (!keywords.isEmpty()) ? totalScore / keywords.size() : 0.0;

            competencyScoreRepository.save(
                    CompetencyScore.builder()
                            .userId(userId)
                            .resumeId(resumeId)
                            .competency(competency)
                            .totalScore(avgScore)
                            .build()
            );
            result.add(CompetencyScoreDTO.builder()
                    .competencyName(competency.getName())
                    .totalScore(avgScore)
                    .keywordScoreDTOS(keywordDTOs)
                    .build());
        }
        return result;
    }
    //전체 조회
    public List<CompetencyScoreDTO> findHistoryScore() {
        List<CompetencyScore> competencyScores = competencyScoreRepository.findAll();
        List<CompetencyScoreDTO> result = new ArrayList<>();
        for (CompetencyScore cs : competencyScores) {
            List<KeywordScore> keywordScores =
                    keywordScoreRepository.findByCompetencyAndResumeIdAndUserId(
                            cs.getCompetency(), cs.getResumeId(), cs.getUserId()
                    );
            List<KeywordScoreDTO> keywordDTOS = new ArrayList<>();
            for (KeywordScore ks : keywordScores) {
                keywordDTOS.add(KeywordScoreDTO.builder()
                        .keywordName(ks.getKeyword().getKeyword())
                        .score(ks.getScore() != null ? ks.getScore() : 0.0)
                        .category(ks.getKeyword().getCategory())
                        .build());
            }
            result.add(CompetencyScoreDTO.builder()
                    .competencyName(cs.getCompetency().getName())
                    .totalScore(cs.getTotalScore() != null ? cs.getTotalScore() : 0.0)
                    .keywordScoreDTOS(keywordDTOS)
                    .build());
        }
        return result;
    }

    //상세 조회(사용자+이력서)
    public List<CompetencyScoreDTO> findHistoryDetailScore(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("이력서 없음"));
        Long userId = resume.getUser().getId();
        List<CompetencyScore> competencyScores =
                competencyScoreRepository.findByUserIdAndResumeId(userId, resumeId);
        List<CompetencyScoreDTO> result = new ArrayList<>();
        for (CompetencyScore cs : competencyScores) {
            List<KeywordScore> keywordScores =
                    keywordScoreRepository.findByCompetencyAndResumeIdAndUserId(
                            cs.getCompetency(), resumeId, userId
                    );
            List<KeywordScoreDTO> keywordDTOS = new ArrayList<>();
            for (KeywordScore ks : keywordScores) {
                keywordDTOS.add(KeywordScoreDTO.builder()
                        .keywordName(ks.getKeyword().getKeyword())
                        .score(ks.getScore() != null ? ks.getScore() : 0.0)
                        .category(ks.getKeyword().getCategory())
                        .build());
            }
            result.add(CompetencyScoreDTO.builder()
                    .competencyName(cs.getCompetency().getName())
                    .totalScore(cs.getTotalScore() != null ? cs.getTotalScore() : 0.0)
                    .keywordScoreDTOS(keywordDTOS)
                    .build());
        }
        return result;
    }
}