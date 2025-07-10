from fastapi import FastAPI, UploadFile, File, Form
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
import uuid, logging, requests

from app.extractor import extract_text
from app.embedding import embed_resume
from app.s3_utils import upload_to_s3, upload_text_to_s3
from app.db_utils import save_resume_info_to_db
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

@app.post("/resume/process")
async def process_resume(
        file: UploadFile = File(...),
        job_field: str = Form(...),
        user_id: int = Form(...),
):
    try:
        text = extract_text(file)
        embedding = embed_resume(text)

        filename = f"{uuid.uuid4()}_{file.filename}"
        file.file.seek(0)
        file_url = upload_to_s3(file.file, f"resumes/{filename}")
        text_url = upload_text_to_s3(text, f"resume_texts/{filename}.txt")
        preview = text[:300]

        resume_id = save_resume_info_to_db(
            user_id=user_id,
            file_url=file_url,
            text_s3_url=text_url,
            preview_text=preview,
            embedding=embedding,
            job_field=job_field
        )

        try:
            spring_response = requests.post(SPRING_API_URL, json={
                "resume_id": resume_id,
                "embedding": embedding,
                "job_field": job_field
            }, timeout=5)

            if spring_response.status_code == 200:
                score_result = spring_response.json().get("score_result")
            else:
                logger.warning(f"Spring 분석 실패: {spring_response.status_code}")
                score_result = None
        except Exception as e:
            logger.error(f"Spring 통신 에러: {e}")
            score_result = None

        return {
            "resume_id": resume_id,
            "file_url": file_url,
            "text_s3_url": text_url,
            "preview_text": preview,
            "score_result": score_result or "분석 결과 없음"
        }

    except Exception as e:
        logger.error(f"전체 처리 실패: {e}")
        return JSONResponse(status_code=500, content={"detail": str(e)})