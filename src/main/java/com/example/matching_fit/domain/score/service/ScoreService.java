package com.example.matching_fit.domain.score.service;

import com.example.matching_fit.domain.score.dto.CompetencyScoreDTO;
import com.example.matching_fit.domain.score.dto.KeywordScoreDTO;
import com.example.matching_fit.domain.score.dto.ResumeEmbeddingRequestDTO;
import com.example.matching_fit.domain.score.entity.Competency;
import com.example.matching_fit.domain.score.entity.CompetencyScore;
import com.example.matching_fit.domain.score.entity.Keyword;
import com.example.matching_fit.domain.score.entity.KeywordScore;
import com.example.matching_fit.domain.score.repository.CompetencyRepository;
import com.example.matching_fit.domain.score.repository.CompetencyScoreRepository;
import com.example.matching_fit.domain.score.repository.KeywordRepository;
import com.example.matching_fit.domain.score.repository.KeywordScoreRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
//인베딩 과정 추가하기(인베딩 아직 못함 / 사유 : 어떤 ai를 쓸껀지(모델), 벡터 생성 및 유사도 계산 로직) //ai = 코볼트, 비교방식은 한번찾아보고
//조금더 알아볼 필요 있음
@Service
@RequiredArgsConstructor
public class ScoreService {
    private final CompetencyRepository competencyRepository;
    private final KeywordRepository keywordRepository;
    //신규 추가
    private final CompetencyScoreRepository competencyScoreRepository;
    private final KeywordScoreRepository keywordScoreRepository;
    // ✅EmbeddingService 사용
    private final EmbeddingService embeddingService;

    //이력서를 받아서 점수로 반환하기
    @Transactional  //추가
    public List<CompetencyScoreDTO> sumScore(Long userId, Long resumeId, ResumeEmbeddingRequestDTO resumeEmbeddingRequestDTO){
        //역량마다 본인의 점수가 매칭
        List<CompetencyScoreDTO> result = new ArrayList<>();
        //⭐️숫자로 변환
        List<Double> resumeEmbedding = resumeEmbeddingRequestDTO.getResumeEmbedding();

        // 각각의 역량의 점수는 0점에서 시작 및 키워드가 나올때마다 점수추가
        for (Competency competency : competencyRepository.findAll()) {
            List<Keyword> keywords = keywordRepository.findByCompetency(competency);
            double totalScore = 0.0;
            List<KeywordScoreDTO> keywordDTOs = new ArrayList<>();

            // 키워드별 점수계산
            for (Keyword keyword : keywords) {
                // 실제 구현에서는 키워드 임베딩을 가져와야 함 (지금은 예시로 resumeEmbedding 사용)
                List<Double> keywordEmbedding = new ArrayList<>(resumeEmbedding);
                // 이력서와 키워드 임베딩 간 코사인 유사도 점수 계산
                double rawScore = embeddingService.cosineSimilarity(resumeEmbedding, keywordEmbedding);

                //가중치 계산
                double weight = keyword.getWeightScore() != null ? keyword.getWeightScore() : 1.0; //  null(아무것도 없음)이면 기본값 1.0을 사용
                double weightedScore = rawScore * weight; // 가중치 = 원래점수 * 가중치

                //키워드별 점수 결과를 KeywordScore에 저장
                keywordScoreRepository.save(
                        KeywordScore.builder()
                                .userId(userId)
                                .resumeId(resumeId)
                                .competencyId(competency.getId())
                                .keywordId(keyword.getId())
                                .score(weightedScore)
                                .build()
                );

                //키워드별로 점수를 모아서, 역량에 값을 넣음
                keywordDTOs.add(KeywordScoreDTO.builder()
                        .keywordName(keyword.getKeyword())
                        .score(weightedScore)
                        .build());
                totalScore += weightedScore;
            }

            //키워드별 점수 합을, 키워드 개수로 나눠서 역량의 평균 점수를 구하며 키워드가 없으면 0점 처리
            double avgScore = !keywords.isEmpty() ? totalScore / keywords.size() : 0.0;

            //역량별 점수 결과를 CompetencyScore에 저장
            competencyScoreRepository.save(
                    CompetencyScore.builder()
                            .userId(userId)
                            .resumeId(resumeId)
                            .competencyId(competency.getId())
                            .totalScore(avgScore)
                            .build()
            );

            //결과를 모음(종합적으로 모음)
            result.add(CompetencyScoreDTO.builder()
                    .competencyName(competency.getName())
                    .totalScore(avgScore)
                    .keywordScoreDTOS(keywordDTOs)
                    .build());
        }
        return result;
    }
//    //전체 조회
//    public List<CompetencyScoreDTO> findHistoryScore(){
//        //엔티티를 리스트로 가져오고
//        //최종적으로 반환할 DTO 리스트 만듬
//        List<Competency> competencies = competencyRepository.findAll();
//        List<CompetencyScoreDTO> result = new ArrayList<>();
//
//        //역량 엔티티들을 하나씩 반복
//        for (Competency competency : competencies) {
//            //역량에 속한 키워드별 점수 DTO리스트를 생성
//            List<KeywordScoreDTO> keywordScoreDTOS = new ArrayList<>();
//            //역량에 속한 키워드들을 하나씩 반복
//            for (Keyword keyword : competency.getKeywords()) {
//                keywordScoreDTOS.add(KeywordScoreDTO.builder()
//                        .keywordName(keyword.getKeyword())
//                        .score(keyword.getScore() != null ? keyword.getScore() : 0.0)
//                        //null이 아니면 그값을 사용 null이면 0을 사용
//                        .build());
//            }
//            //역량의 정보를 DTO로 변환해서 결과 리스트에 추가
//            result.add(CompetencyScoreDTO.builder()
//                    .competencyName(competency.getName())
//                    .totalScore(competency.getTotalScore() != null ? competency.getTotalScore() : 0.0)
//                    //null이 아니면 그값을 사용 null이면 0을 사용
//                    .keywordScoreDTOS(keywordScoreDTOS)
//                    .build());
//        }
//        return result;
//    }
//
//    //상세 조회
//    public List<CompetencyScoreDTO> findHistoryDetailScore(Long userId, Long resumeId){
//        List<Competency> competencies = competencyRepository.findByUserIdAndResumeId(userId, resumeId);
//        List<CompetencyScoreDTO> result = new ArrayList<>();
//        for (Competency competency : competencies) {
//            List<KeywordScoreDTO> keywordScoreDTOS = new ArrayList<>();
//            for (Keyword keyword : competency.getKeywords()){
//                keywordScoreDTOS.add(KeywordScoreDTO.builder()
//                        .keywordName(keyword.getKeyword())
//                        .score(keyword.getScore() != null ? keyword.getScore() : 0.0)
//                        .build());
//            }
//            result.add(CompetencyScoreDTO.builder()
//                    .competencyName(competency.getName())
//                    .totalScore(competency.getTotalScore() != null ? competency.getTotalScore() : 0.0)
//                    .keywordScoreDTOS(keywordScoreDTOS)
//                    .build());
//
//        }
//        return result;
//    }
}