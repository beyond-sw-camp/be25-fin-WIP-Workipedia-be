# ADR 009 - Elasticsearch Strategy

> 문서 유형: ADR
> 상태: Draft
> 정본 위치: `docs/003-adr/009-elasticsearch-strategy.md`
> 관련 문서: `docs/003-adr/002-rag-strategy.md`, `docs/001-reference/trd.md`, `docs/010-development/domain-guides/chatbot-rag.md`
> 버전: v0.1
> 최종 수정: 2026-06-01

## Context

RAG 파이프라인에서 매뉴얼/워키 chunk를 저장하고 유사도 검색을 수행할 Vector Store가 필요하다.

TRD에서는 pgvector / OpenSearch를 후보로 언급했으며, V1 스키마는 `embedding_json` 컬럼을 RDB에 두는 최소 구현으로 시작했다.

팀 논의 결과, 검색 품질과 확장성을 고려해 Elasticsearch를 Vector Store로 채택하기로 했다.

담당: 민정기

## Decision

Vector Store로 **Elasticsearch**를 사용한다.

- docker-compose에 Elasticsearch 컨테이너를 추가한다.
- 매뉴얼/워키 chunk를 Elasticsearch에 인덱싱한다.
- 유사도 검색은 Elasticsearch의 kNN(k-nearest neighbor) 검색을 사용한다.
- Spring 애플리케이션에서는 adapter 패턴으로 격리한다 (`rag/adapter/VectorSearchClient`).
- RDB의 `embedding_json` 컬럼은 fallback 또는 메타데이터 저장 목적으로 유지한다.

## Consequences

- RAG 검색 품질이 RDB 기반보다 향상된다.
- docker-compose에 Elasticsearch 서비스가 추가되어 로컬 환경 요구사항이 늘어난다.
- 민정기가 Elasticsearch 인덱스 설계와 adapter 구현을 담당한다.
- 김진혁의 RAG 흐름(`RagAnswerService`)이 `VectorSearchClient` adapter를 통해 Elasticsearch와 연결된다.
- Elasticsearch 장애 시 RDB 기반 fallback 경로를 고려할 수 있다.

## Open Questions

- Elasticsearch 버전 확정 필요 (8.x 권장).
- kNN 인덱스 설정(차원수, similarity 함수) 확정 필요 — 사용할 로컬 임베딩 모델 확정 후 결정.
- RDB `embedding_json` 컬럼을 MVP 이후 제거할지, fallback으로 유지할지 결정 필요.
- 로컬 임베딩 모델 확정은 ADR 002 Open Questions 참조.
