# ADR 009 - Elasticsearch Strategy

> 문서 유형: ADR
> 상태: Accepted
> 정본 위치: `docs/adr/009-elasticsearch-strategy.md`
> 관련 문서: `docs/adr/002-rag-strategy.md`, `docs/reference/trd.md`, `docs/dev/domain-guides/chatbot-rag.md`
> 버전: v0.3
> 최종 수정: 2026-06-12

## Context

Workipedia BE는 워키·매뉴얼 등 서비스 데이터의 전문 검색과 검색 통계를 제공해야 한다. 동시에 AI 서버는 RAG와 티켓 라우팅 후보 검색을 위한 독립 Vector Store가 필요하다.

두 저장소의 책임을 섞으면 인덱스 스키마와 장애 범위가 결합되므로 경계를 분리한다.

## Decision

- Elasticsearch는 BE의 전문 검색과 BE 소유 검색 기능에 사용한다.
- Qdrant는 AI 서버의 RAG·라우팅 Vector Store로 사용한다.
- Elasticsearch를 AI RAG의 fallback Vector Store로 사용하지 않는다.
- Qdrant를 BE 전문 검색이나 검색 통계 용도로 사용하지 않는다.
- RDB의 `embedding_json` 컬럼은 현재 migration 호환을 위해 유지하되 신규 AI 검색의 정본으로 사용하지 않는다.

```text
BE: MariaDB → Elasticsearch
AI: BE 동기화 이벤트/API → chunking/embedding → Qdrant
```

## Consequences

- BE 검색과 AI RAG의 인덱스 수명주기와 장애를 독립적으로 관리할 수 있다.
- 동일 원문이 두 검색 저장소에 필요한 경우 동기화 계약과 삭제 보상 처리가 필요하다.
- Elasticsearch 장애는 BE 검색 기능에, Qdrant 장애는 AI RAG·라우팅에 각각 영향을 준다.
- 두 저장소 모두 MariaDB 정본에서 독립적으로 재구축한다.

## Open Questions

- 동일 문서의 BE 검색 색인과 AI Vector Store 동기화 순서
- 삭제·수정 이벤트 재처리와 정합성 점검 주기
- RDB `embedding_json` 컬럼의 장기 제거 시점
