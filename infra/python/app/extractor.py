import pdfplumber
import docx
from fastapi import UploadFile

def extract_text(file: UploadFile) -> str:
    if file.filename.endswith(".pdf"):
        with pdfplumber.open(file.file) as pdf:
            return "\n".join(page.extract_text() for page in pdf.pages if page.extract_text())
    elif file.filename.endswith(".docx"):
        doc = docx.Document(file.file)
        return "\n".join([p.text for p in doc.paragraphs])
    else:
        raise ValueError("지원하지 않는 파일 형식입니다.")