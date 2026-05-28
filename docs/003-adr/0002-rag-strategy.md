# ADR 0002 - RAG Strategy

> 문서 유형: ADR
> 상태: Draft
> 정본 위치: `docs/003-adr/0002-rag-strategy.md`
> 관련 문서: `docs/001-reference/constitution.md`, `docs/001-reference/trd.md`, `docs/007-quality/harness-engineering.md`
> 버전: v0.1
> 최종 수정: 2026-05-28

## Context

발표 일정은 2026-07-03이고, 배포 목표일은 2026-06-26이다. 팀은 완성도 높은 프로젝트를 목표로 하며, 챗봇이 단순 mock처럼 보이지 않도록 로컬 LLM/임베딩 기반 검색 흐름을 구현하고자 한다.

헌법상 챗봇 답변은 출처가 있어야 하며, 근거가 없으면 답변을 꾸며내지 않아야 한다. 따라서 RAG의 핵심은 "멋진 답변 생성"보다 "검색 근거, 출처, 실패 전환, 감사 가능성"이다.

## Decision

MVP RAG 전략은 **local embedding first, mock fallback**으로 한다.

우선 구현 대상:

- seed 매뉴얼/워키 문서 10~20개 준비
- 로컬 임베딩 모델로 문서 chunk embedding 생성
- 질문 embedding 생성
- top-k 유사 chunk 검색
- 검색된 chunk 기반 답변 생성
- 답변에 출처 포함
- `chatbot_messages.references` JSON 저장
- 근거 부족 시 워키 질문 또는 요청 티켓 전환 액션 반환
- 개인정보 마스킹 기본 케이스
- 로컬 모델 또는 임베딩 실패 시 mock RAG fallback

### 구현 깊이

| 영역 | MVP 기준 | 후순위 |
|---|---|---|
| 임베딩 | 로컬 모델 호출 또는 로컬 스크립트 기반 embedding 생성 | 고성능 모델 튜닝 |
| Vector Store | MariaDB 테이블 또는 단순 in-memory/vector table adapter | pgvector/OpenSearch 운영 |
| 검색 | cosine similarity top-k | hybrid search, reranking |
| 답변 생성 | 검색 chunk 기반 template/local LLM 응답 | 고품질 프롬프트 튜닝 |
| 출처 | 문서 ID, chunk ID, 제목, 링크 저장 | 문단 단위 deep link |
| 실패 처리 | no-answer + 워키 질문/요청 티켓 전환 | confidence calibration 고도화 |

## Consequences

- 발표에서 실제 검색 기반 답변 흐름을 보여줄 수 있다.
- 외부 API 키 없이도 RAG 구조를 검증할 수 있다.
- 품질 고도화보다 "근거 있는 답변"과 "업무 전환"을 우선한다.
- 로컬 모델 연동이 늦어져도 mock fallback으로 시연 흐름을 유지할 수 있다.
- Vector Store 운영 복잡도는 낮추되, 추후 pgvector/OpenSearch로 교체 가능한 adapter 구조가 필요하다.

## Local RAG Flow

```text
seed manual/worki data
-> chunking
-> local embedding generation
-> vector storage
-> user question
-> question embedding
-> top-k retrieval
-> answer generation
-> reference validation
-> chatbot_messages.references 저장
-> no-answer이면 워키 질문 또는 요청 티켓 전환
```

## Open Questions

- 로컬 임베딩 모델 후보 확정 필요.
- 로컬 LLM을 직접 붙일지, 검색 결과 기반 template 답변을 먼저 쓸지 결정 필요.
- 벡터 저장을 MariaDB 테이블로 최소 구현할지, 별도 local vector store를 둘지 결정 필요.
- seed 문서 범위와 개수 결정 필요.
