from fastapi import FastAPI, Query
from fastapi.responses import JSONResponse
from starlette.middleware.cors import CORSMiddleware
import boto3, uuid, os

AWS_ACCESS_KEY_ID = os.getenv("AWS_ACCESS_KEY_ID")
AWS_SECRET_ACCESS_KEY = os.getenv("AWS_SECRET_ACCESS_KEY")
AWS_REGION = os.getenv("AWS_REGION")
S3_BUCKET_NAME = os.getenv("S3_BUCKET_NAME")

# ✅ 2. FastAPI 초기화
app = FastAPI()

# ✅ 3. CORS 미들웨어 설정 (React 개발용) - 포트 5174 추가
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:3000",
        "http://127.0.0.1:3000",
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ✅ 4. MIME 타입 매핑 함수
def get_mime_type(file_ext: str) -> str:
    ext_to_mime = {
        "pdf": "application/pdf",
        "docx": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "png": "image/png",
        "jpg": "image/jpeg",
        "jpeg": "image/jpeg",
        "txt": "text/plain"
    }
    return ext_to_mime.get(file_ext.lower(), f"application/octet-stream")  # 기본값

# ✅ 5. boto3 S3 클라이언트 초기화
s3 = boto3.client(
    "s3",
    region_name=AWS_REGION,
    aws_access_key_id=AWS_ACCESS_KEY_ID,
    aws_secret_access_key=AWS_SECRET_ACCESS_KEY
)

# ✅ 6. presigned URL 발급 API
@app.get("/get-presigned-url")
def get_presigned_url(file_ext: str = Query(..., description="파일 확장자 예: pdf, docx")):
    try:
        mime_type = get_mime_type(file_ext)
        unique_filename = f"{uuid.uuid4()}.{file_ext}"
        s3_key = f"resumes/{unique_filename}"

        presigned_url = s3.generate_presigned_url(
            ClientMethod="put_object",
            Params={
                "Bucket": S3_BUCKET_NAME,
                "Key": s3_key,
                "ContentType": mime_type
            },
            ExpiresIn=300  # 5분
        )

        print("S3 Key:", s3_key)
        print("Signed ContentType:", mime_type)
        print("Presigned URL:", presigned_url)

        file_url = f"https://{S3_BUCKET_NAME}.s3.{AWS_REGION}.amazonaws.com/{s3_key}"

        return {
            "presigned_url": presigned_url,
            "file_url": file_url,
            "mime_type": mime_type  # 디버깅 시에도 도움 됨
        }

    except Exception as e:
        return JSONResponse(status_code=500, content={"detail": str(e)})