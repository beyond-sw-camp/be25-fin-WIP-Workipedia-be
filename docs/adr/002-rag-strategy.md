# ADR 002 - RAG Strategy

> 문서 유형: ADR
> 상태: Draft
> 정본 위치: `docs/003-adr/002-rag-strategy.md`
> 관련 문서: `docs/001-reference/constitution.md`, `docs/001-reference/trd.md`, `docs/007-quality/harness-engineering.md`
> 버전: v0.2
> 최종 수정: 2026-06-04

## Context

발표 일정은 2026-07-03이고, 배포 목표일은 2026-06-26이다. 팀은 완성도 높은 프로젝트를 목표로 하며, 챗봇이 단순 mock처럼 보이지 않도록 로컬 LLM/임베딩 기반 검색 흐름을 구현하고자 한다.

헌법상 챗봇 답변은 출처가 있어야 하며, 근거가 없으면 답변을 꾸며내지 않아야 한다. 따라서 RAG의 핵심은 "멋진 답변 생성"보다 "검색 근거, 출처, 실패 전환, 감사 가능성"이다.

## Decision

MVP RAG 전략은 **local embedding first, mock fallback**으로 한다.

장기 AI 전략은 **RAG + QLoRA 혼합 아키텍처**로 한다.

- RAG는 매뉴얼/워키/지식화 문서 검색과 출처 기반 답변 정확성을 담당한다.
- QLoRA는 지식을 외우는 용도가 아니라 사내 특화 행동 패턴을 학습하는 용도로 제한한다.
- 행동 패턴에는 근거 부족 시 거절, 워키/티켓 전환 유도, 출처 표시 형식, 개인정보성 질문 처리 방식이 포함된다.
- 따라서 QLoRA 학습 데이터에는 최신 지식 자체보다 답변 정책과 처리 흐름을 반영한다.

우선 구현 대상:

- seed 매뉴얼/워키 문서 10~20개 준비
- 로컬 임베딩 모델로 문서 chunk embedding 생성
- 질문 embedding 생성
- top-k 유사 chunk 검색
- 검색된 chunk 기반 답변 생성
- 답변에 출처 포함
- `chatbot_messages.references_json` JSON 저장
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
| 행동 튜닝 | 시스템 프롬프트 기반 제어 | APPROVED 데이터 기반 QLoRA |
| 출처 | 문서 ID, chunk ID, 제목, 링크 저장 | 문단 단위 deep link |
| 실패 처리 | no-answer + 워키 질문/요청 티켓 전환 | confidence calibration 고도화 |

## Consequences

- 발표에서 실제 검색 기반 답변 흐름을 보여줄 수 있다.
- 외부 API 키 없이도 RAG 구조를 검증할 수 있다.
- 품질 고도화보다 "근거 있는 답변"과 "업무 전환"을 우선한다.
- 로컬 모델 연동이 늦어져도 mock fallback으로 시연 흐름을 유지할 수 있다.
- Vector Store 운영 복잡도는 낮추되, 추후 pgvector/OpenSearch로 교체 가능한 adapter 구조가 필요하다.
- 지식은 RAG로 최신성을 유지하고, QLoRA는 행동만 학습시켜 hallucination 위험을 줄인다.
- base_system 프롬프트를 바꾸면 재학습 트리거가 필요할 수 있고, admin_context는 런타임에 즉시 반영한다.

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
-> chatbot_messages.references_json 저장
-> no-answer이면 워키 질문 또는 요청 티켓 전환
```

## QLoRA Experiment Summary

| 실험 | 조건 | 결과 |
|---|---|---|
| 1차 | 데이터 15개, iters 100 | Val Loss 3.889 → 0.742 |
| 2차 | 데이터 50개, iters 300 | Val Loss 3.889 → 0.360, iter 200 이후 과적합 조짐 |

실험 결과, 소량 데이터로도 거절 방식과 전환 유도 같은 행동 패턴 학습은 가능했다. 다만 지식 자체를 학습시키면 hallucination 위험이 남기 때문에 지식 주입은 RAG로 제한한다.

## Prompt and Fine-Tuning Policy

| 영역 | 역할 | 수정 시 재학습 | 편집 권한 |
|---|---|---|---|
| `base_system` | QLoRA 학습 기준 프롬프트, 핵심 행동 규칙 | 필요 | SYSTEM_ADMIN |
| `admin_context` | 회사/부서 맞춤 런타임 지침 | 불필요 | SYSTEM_ADMIN |

런타임 프롬프트:

```text
final_system_prompt = base_system + "\n\n" + admin_context
```

학습 데이터 소스:

| 소스 | 조건 |
|---|---|
| `worki_answers` | `official = true` |
| `knowledge_candidates` | `status = APPROVED` + 유효기간 이내 |

유효한 학습 데이터가 임계값(기본 100건)을 넘으면 JSONL 변환 후 QLoRA 파인튜닝을 실행한다. 완료 후 어댑터를 교체하고 챗봇에 반영한다.

출처 최신성 표시:

| 경과 기간 | 표시 |
|---|---|
| 3개월 이하 | 별도 표시 없음 |
| 3개월 ~ 6개월 | "N개월 된 내용입니다" |
| 6개월 초과 | "오래된 내용일 수 있습니다. 확인 후 참고하세요." |

## Open Questions

- 로컬 임베딩 모델 후보 확정 필요.
- 로컬 LLM을 직접 붙일지, 검색 결과 기반 template 답변을 먼저 쓸지 결정 필요.
- 벡터 저장을 MariaDB 테이블로 최소 구현할지, 별도 local vector store를 둘지 결정 필요.
- seed 문서 범위와 개수 결정 필요.
- QLoRA 자동 파인튜닝을 MVP에 포함할지, Phase 2로 둘지 결정 필요.
- 학습 데이터 유효기간 기본값을 6개월로 둘지 결정 필요.
