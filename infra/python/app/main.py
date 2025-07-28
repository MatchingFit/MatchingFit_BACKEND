from fastapi import FastAPI, UploadFile, File, Form
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
import uuid, logging, httpx, asyncio

from app.extractor import extract_text
from app.embedding import embed_resume
from app.s3_utils import upload_to_s3, upload_text_to_s3
from app.db_utils import save_resume_info_to_db
from app.elastic_utils import delete_keyword_index_if_exists, create_keyword_index_if_needed, index_keywords_batch
from app.config import SPRING_API_URL

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI()
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.post("/initialize_keywords")
def initialize_keywords():
    delete_keyword_index_if_exists()
    create_keyword_index_if_needed()
    index_keywords_batch()
    return {"status": "success"}

@app.post("/resume/process")
async def process_resume(
        file: UploadFile = File(...),
        job_field: str = Form(...),
        user_id: str = Form(...),
):
    try:
        user_id_int = int(user_id)

        text = extract_text(file)
        embedding = embed_resume(text)

        filename = f"{uuid.uuid4()}_{file.filename}"
        file.file.seek(0)
        file_url = upload_to_s3(file.file, f"resumes/{filename}")
        text_url = upload_text_to_s3(text, f"resume_texts/{filename}.txt")
        preview = text[:300]

        resume_id = save_resume_info_to_db(
            user_id=user_id_int,
            file_url=file_url,
            text_s3_url=text_url,
            preview_text=preview,
            job_field=job_field
        )

        logger.info(f"🚀 이력서 분석 시작: resumeId={resume_id}, 임베딩 길이={len(embedding)}")

        async with httpx.AsyncClient() as client:
            # 1. 점수 계산 API와 OpenAI 분석 API를 병렬로 호출
            logger.info("📡 Spring Boot API 병렬 호출 시작")
            score_task = client.post(
                SPRING_API_URL + "/api/score/total",
                json={
                    "resumeId": resume_id,
                    "embedding": embedding
                },
                timeout=100
            )
            
            analyze_task = client.get(
                SPRING_API_URL + f"/api/v1/gpt/resumes/{resume_id}/analyze",
                timeout=100
            )
            
            # 두 요청을 동시에 실행하고 결과 대기
            score_spring_response, analyze_spring_response = await asyncio.gather(
                score_task, 
                analyze_task,
                return_exceptions=True
            )
            
            # 점수 계산 결과 처리
            if isinstance(score_spring_response, Exception):
                logger.error(f"점수 계산 API 호출 실패: {score_spring_response}")
                score_result = None
            elif score_spring_response.status_code == 200:
                competency_scores = score_spring_response.json()
                logger.info(f"✅ 점수 계산 완료: {len(competency_scores)}개 역량")

                # 역량별 총점과 기술 전문성 키워드만 추출
                competency_totals = []
                technical_keywords_by_category = {}
                
                for competency in competency_scores:
                    competency_name = competency.get('competencyName', 'Unknown')
                    total_score = competency.get('totalScore', 0)
                    logger.info(f"📊 역량: {competency_name}, 총점: {total_score:.2f}")

                    # 역량별 총점 추가
                    competency_totals.append({
                        "competencyName": competency_name,
                        "totalScore": total_score
                    })
                    
                    # 기술 전문성 키워드를 카테고리별로 그룹핑
                    if competency_name == "기술 전문성" and 'keywordScoreDTOS' in competency and competency['keywordScoreDTOS']:
                        keywords = competency['keywordScoreDTOS']
                        logger.info(f"🔧 기술 키워드 개수: {len(keywords)}개")

                        # 카테고리별로 그룹핑
                        for keyword in keywords:
                            category = keyword.get('category', 'UNKNOWN')
                            # Category enum이 label 값으로 직렬화됨 (예: "백엔드", "프론트엔드" 등)
                            if category not in technical_keywords_by_category:
                                technical_keywords_by_category[category] = []
                            technical_keywords_by_category[category].append(keyword)
                
                # 카테고리별 키워드 개수 로그
                for category, keywords in technical_keywords_by_category.items():
                    logger.info(f"📁 {category}: {len(keywords)}개 키워드")

                # 최종 결과 구성
                score_result = {
                    "competencyScores": competency_totals,
                    "technicalKeywords": technical_keywords_by_category
                }
                logger.info(f"✅ 점수 결과 구성 완료: {len(competency_totals)}개 역량, {len(technical_keywords_by_category)}개 카테고리")
            else:
                score_result = None
            
            # OpenAI 분석 결과 처리
            if isinstance(analyze_spring_response, Exception):
                analyze_result = None
            elif analyze_spring_response.status_code == 200:
                analyze_result = analyze_spring_response.json()
            else:
                analyze_result = None


        logger.info(f"✅ 이력서 분석 완료: resumeId={resume_id}")
        
        return {
            "resume_id": resume_id,
            "file_url": file_url,
            "text_s3_url": text_url,
            "preview_text": preview,
            "job_field": job_field,
            "score_result": score_result or "분석 결과 없음",
            "ai_analysis": analyze_result or "AI 분석 결과 없음"
        }

    except Exception as e:
        logger.error(f"전체 처리 실패: {e}")
        return JSONResponse(status_code=500, content={"detail": str(e)})

