from elasticsearch import Elasticsearch, helpers
from app.config import ELASTIC_HOST, ELASTIC_PORT
from app.embedding import embed_keywords_batch
from app.db_utils import get_all_keywords_for_embedding, SessionLocal

es = Elasticsearch([
    {"host": ELASTIC_HOST, "port": int(ELASTIC_PORT), "scheme": "http"}
])
KEYWORD_INDEX = "keywords"

def create_keyword_index_if_needed():
    try:
        info = es.info()
        print("Elasticsearch info:", info)
    except Exception as e:
        print("Elasticsearch 연결 실패:", e)

    if not es.indices.exists(index=KEYWORD_INDEX):
        try:
            es.indices.create(
                index=KEYWORD_INDEX,
                body={
                    "mappings": {
                        "properties": {
                            "keyword": {"type": "keyword"},
                            "embedding": {
                                "type": "dense_vector",
                                "dims": 768
                            },
                            "competency_id": {"type": "integer"},
                            "weight_score": {"type": "float"}
                        }
                    }
                }
            )
            print("✅ 인덱스 생성 완료")
        except Exception as e:
            print(f"❌ 인덱스 생성 중 에러 발생: {e}")
            raise


def index_keywords_batch():
    session = SessionLocal()
    try:
        keyword_entries = get_all_keywords_for_embedding(session)
        texts = [k["keyword"] for k in keyword_entries]
        embeddings = embed_keywords_batch(texts)

        actions = [
            {
                "_index": KEYWORD_INDEX,
                "_id": str(entry["id"]),
                "_source": {
                    "keyword": entry["keyword"],
                    "embedding": embedding,
                    "competency_id": entry["competency_id"],
                    "weight_score": entry["weight_score"],
                },
            }
            for entry, embedding in zip(keyword_entries, embeddings)
        ]

        response = helpers.bulk(es, actions)
        print("Bulk 인덱싱 결과:", response)
    finally:
        session.close()