import psycopg2
import logging
from app.config import DATABASE_URL
from sqlalchemy import create_engine, Column, Integer, String, Float, ForeignKey
from sqlalchemy.orm import sessionmaker, relationship, declarative_base

logger = logging.getLogger(__name__)

# SQLAlchemy 세팅
Base = declarative_base()

class Competency(Base):
    __tablename__ = "competencies"
    id = Column(Integer, primary_key=True)
    name = Column(String)
    keywords = relationship("Keyword", back_populates="competency")

class Keyword(Base):
    __tablename__ = "keywords"
    id = Column(Integer, primary_key=True)
    keyword = Column(String)
    weight_score = Column(Float)
    competency_id = Column(Integer, ForeignKey("competencies.id"))
    competency = relationship("Competency", back_populates="keywords")

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(bind=engine)

def get_all_keywords_for_embedding(db):
    try:
        keywords = db.query(Keyword).join(Keyword.competency).all()
        result = []
        for k in keywords:
            result.append({
                "id": k.id,
                "keyword": k.keyword,
                "competency_id": k.competency.id,
                "weight_score": k.weight_score
            })
        return result
    except Exception as e:
        logger.exception("[키워드 조회 실패]")
        raise

def save_resume_info_to_db(
        user_id: int,
        file_url: str,
        text_s3_url: str,
        preview_text: str,
        job_field: str
) -> int:
    try:
        with psycopg2.connect(DATABASE_URL) as conn:
            with conn.cursor() as cursor:
                cursor.execute("""
                    INSERT INTO resumes (
                        user_id, file_url, text_s3_url, preview_text, job_field
                    ) VALUES (%s, %s, %s, %s, %s)
                    RETURNING id
                """, (user_id, file_url, text_s3_url, preview_text, job_field))
                resume_id = cursor.fetchone()[0]
                return resume_id
    except Exception as e:
        logger.exception("[DB 저장 실패]")
        raise
