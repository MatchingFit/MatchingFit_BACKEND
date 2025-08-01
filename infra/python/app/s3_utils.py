import boto3
from app.config import AWS_REGION, AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, S3_BUCKET_NAME

s3 = boto3.client(
    "s3",
    region_name=AWS_REGION,
    aws_access_key_id=AWS_ACCESS_KEY_ID,
    aws_secret_access_key=AWS_SECRET_ACCESS_KEY,
)


def upload_to_s3(file, filename):
    s3.upload_fileobj(file, S3_BUCKET_NAME, filename)
    return f"https://{S3_BUCKET_NAME}.s3.{AWS_REGION}.amazonaws.com/{filename}"

def upload_text_to_s3(text, filename):
    s3.put_object(Bucket=S3_BUCKET_NAME, Key=filename, Body=text.encode("utf-8"))
    return f"https://{S3_BUCKET_NAME}.s3.{AWS_REGION}.amazonaws.com/{filename}"

def upload_pdf_to_s3(file_obj, filename):
    s3.put_object(Bucket=S3_BUCKET_NAME, Key=filename, Body=file_obj, ContentType="application/pdf")
    return f"https://{S3_BUCKET_NAME}.s3.{AWS_REGION}.amazonaws.com/{filename}"