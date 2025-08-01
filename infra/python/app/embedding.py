from app.models.embedding_model import e5_model as e5_model

def embed_resume(text: str) -> list[float]:
    input_text = f"passage: {text}"
    embedding = e5_model.encode(input_text, normalize_embeddings=True)
    return embedding.tolist()

def embed_keywords_batch(texts: list[str]) -> list[list[float]]:
    input_texts = [f"passage: {t}" for t in texts]
    return e5_model.encode(input_texts, normalize_embeddings=True).tolist()