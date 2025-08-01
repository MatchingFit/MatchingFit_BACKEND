from fastapi import FastAPI, UploadFile, File, Form
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
import uuid, logging, httpx

from app.extractor import extract_text
from app.embedding import embed_resume
from app.s3_utils import upload_to_s3, upload_text_to_s3
from app.db_utils import save_resume_info_to_db
from app.elastic_utils import create_keyword_index_if_needed, index_keywords_batch
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
            embedding=embedding,
            job_field=job_field
        )

        async with httpx.AsyncClient() as client:
            spring_response = await client.post(
                SPRING_API_URL,
                json={
                    "resume_id": resume_id,
                    "embedding": embedding,
                    "job_field": job_field
                },
                timeout=5
            )

            if spring_response.status_code == 200:
                score_result = spring_response.json().get("score_result")
            else:
                logger.warning(f"Spring 분석 실패: {spring_response.status_code}")
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

