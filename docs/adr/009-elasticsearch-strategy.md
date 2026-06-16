# ADR 009 - Elasticsearch Strategy

> 문서 유형: ADR
> 상태: Accepted
> 정본 위치: `docs/adr/009-elasticsearch-strategy.md`
> 관련 문서: `docs/adr/002-rag-strategy.md`, `docs/reference/trd.md`, `docs/dev/domain-guides/chatbot-rag.md`
> 버전: v0.4
> 최종 수정: 2026-06-15

## Context

Workipedia BE는 워키·매뉴얼 등 서비스 데이터의 전문 검색과 검색 통계를 제공해야 한다. 동시에 AI 서버는 RAG와 티켓 라우팅 후보 검색을 위한 독립 Vector Store가 필요하다.

두 저장소의 책임을 섞으면 인덱스 스키마와 장애 범위가 결합되므로 경계를 분리한다.

## Decision

- Elasticsearch는 BE의 전문 검색과 BE 소유 검색 기능에 사용한다.
- Qdrant는 AI 서버의 RAG·라우팅 Vector Store로 사용한다.
- Elasticsearch를 AI RAG의 fallback Vector Store로 사용하지 않는다.
- Qdrant를 BE 전문 검색이나 검색 통계 용도로 사용하지 않는다.
- RDB의 `embedding_json` 컬럼은 현재 migration 호환을 위해 유지하되 신규 AI 검색의 정본으로 사용하지 않는다.
- **워키 질문 검색은 Elasticsearch로 색인·검색한다.**
- **매뉴얼 검색은 현시점 DB(MariaDB) 제목·본문 LIKE 검색으로 처리한다.** 매뉴얼은 ① 업로드 규모가 작고, ② 갱신이 잦지 않으며, ③ 현재 요구가 제목/단순 포함 검색 수준이라, ES 색인·동기화 비용 대비 이득이 낮다고 판단했다. 본문 전문검색(관련도 랭킹·한국어 형태소·대용량 본문)이 필요해지거나 매뉴얼 규모가 커지면 Elasticsearch 색인으로 이행한다(이행 경로는 워키 색인 패턴을 재사용).

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
