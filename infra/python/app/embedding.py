import torch
from app.models.kobert_loader import tokenizer, model

def chunk_text(text: str, max_length: int = 300) -> list[str]:
    sentences = text.split('\n')
    chunks, current_chunk = [], ""
    for sentence in sentences:
        if len(current_chunk) + len(sentence) < max_length:
            current_chunk += sentence + " "
        else:
            chunks.append(current_chunk.strip())
            current_chunk = sentence + " "
    if current_chunk:
        chunks.append(current_chunk.strip())
    return chunks


def get_kobert_embedding(text: str) -> torch.Tensor:
    inputs = tokenizer(text, return_tensors="pt", truncation=True, max_length=512)
    with torch.no_grad():
        outputs = model(**inputs)
        return outputs.pooler_output.squeeze(0)


def embed_resume(text: str) -> list[float]:
    chunks = chunk_text(text)
    if not chunks:
        raise ValueError("텍스트 청크가 없습니다.")
    vectors = [get_kobert_embedding(chunk) for chunk in chunks]
    avg_embedding = torch.mean(torch.stack(vectors), dim=0)
    return avg_embedding.tolist()
