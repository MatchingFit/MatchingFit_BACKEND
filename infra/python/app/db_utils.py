import psycopg2
import logging
from app.config import DATABASE_URL

logger = logging.getLogger(__name__)

def save_resume_info_to_db(
        user_id: int,
        file_url: str,
        text_s3_url: str,
        preview_text: str,
        embedding: list[float],
        job_field: str
) -> int:
    try:
        with psycopg2.connect(DATABASE_URL) as conn:
            with conn.cursor() as cursor:
                cursor.execute("""
                    INSERT INTO resumes (
                        user_id, file_url, text_s3_url, preview_text,
                        embedding, job_field
                    ) VALUES (%s, %s, %s, %s, %s, %s)
                    RETURNING id
                """, (user_id, file_url, text_s3_url, preview_text, embedding, job_field))
                resume_id = cursor.fetchone()[0]
                return resume_id
    except Exception as e:
        logger.exception("[DB 저장 실패]")
        raise
