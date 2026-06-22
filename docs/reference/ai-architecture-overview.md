# Workipedia AI Architecture Overview

> 문서 유형: Architecture Overview
> 상태: Draft
> 정본 위치: `docs/reference/ai-architecture-overview.md`
> 관련 문서: `docs/adr/002-rag-strategy.md`, `docs/adr/008-local-llm-security-strategy.md`, `docs/reference/trd.md`
> 최종 수정: 2026-06-17

## 핵심 원칙

- 지식 제공은 RAG로 통일한다.
- QLoRA와 LangGraph는 사용하지 않는다.
- 고객사별로 별도 배포하며 로컬/클라우드 차이는 provider 추상화와 배포 설정으로 처리한다.
- BE RDB는 민감정보를 암호화 저장하고 사용 시 복호화한다. AI 서버는 LLM 입력과 Vector Store에는 원문을 사용하고, 사용자에게 반환하는 최종 LLM 응답에만 마스킹을 적용한다.
- 구조화된 실시간 데이터는 Tool Calling으로 조회한다.
- AI는 담당 부서까지만 추천하고 개인 담당자는 TEAM_ADMIN이 배정한다.

## 폴백 파이프라인

폴백 순서: A 매뉴얼 → B 워키 → C 지식 RAG → D Tool Calling

```text
A. 매뉴얼 RAG
→ NO_RESULT 또는 ERROR
B. 워키 RAG
→ NO_RESULT 또는 ERROR
C. 지식 RAG
   - TEAM_ADMIN 승인 지식화 게시판(`KNOWLEDGE_DATA`)
   - SYSTEM_ADMIN 수기 지식(`MANUAL_KNOWLEDGE`)
→ NO_RESULT 또는 ERROR
D. 등록된 API/DB Query Tool 호출
→ NO_RESULT 또는 ERROR
요청 티켓 생성 전환 액션
```

해결된 티켓 이력은 별도 단계가 아니며 TEAM_ADMIN 승인 지식화 게시판(C)으로만 검색에 반영한다. `knowledge_data`와 `manual_knowledge`는 DB·`sourceType`·collection을 분리하고, C단계에서 두 collection의 후보를 합쳐 통합 reranking한다.

구현은 명시적인 Python `for` loop와 `if-else`를 사용한다.

```python
for route in route_order:
    result = execute(route, request)
    if result.status == "SUCCESS":
        return result
    if result.status == "BLOCKED":
        return safe_response(result)

return create_transition_action(request)
```

각 단계는 자유 텍스트가 아니라 공통 실행 상태를 반환한다.

| 상태 | 의미 | 다음 처리 |
|---|---|---|
| `SUCCESS` | 유효한 근거나 Tool 결과로 답변 완료 | 응답 반환 |
| `NO_RESULT` | 근거 또는 결과 부족 | 다음 단계 |
| `ERROR` | timeout, 연결 실패 등 실행 오류 | 기록 후 다음 단계 |
| `BLOCKED` | 보안·권한·입력 검증 실패 | 즉시 안전 응답 |

LLM 응답 문자열에서 특정 문구를 찾아 fallback 여부를 판단하지 않는다.

## Negative Answer

다음 중 하나면 RAG 결과를 `NO_RESULT`로 처리한다.

- 검색된 chunk가 없음
- Cross-Encoder 최고 점수가 설정 임계값 미만
- 유효한 출처가 없음
- 생성 답변의 인용 ID가 검색 결과와 일치하지 않음
- 구조화된 생성 결과가 `INSUFFICIENT_CONTEXT`를 반환

최종 응답 마스킹 실패나 허용되지 않은 Tool 입력은 `BLOCKED`로 처리한다.

## 주요 컴포넌트

```text
API Layer
└─ Orchestrator
   ├─ SensitiveDataMasker
   ├─ ManualRagStep
   ├─ WorkiRagStep
   ├─ KnowledgeRagStep
   ├─ ToolCallingStep
   ├─ ChatbotService
   ├─ ManualKnowledgeIndexer
   ├─ ToolSelector
   ├─ DepartmentRoutingService
   ├─ CrossEncoderReranker
   ├─ LlmProvider
   └─ EmbeddingProvider
```

Reranker는 각 후보의 `candidateId`, 원본 `score`, `rank`를 반환한다. 라우팅 서비스는 이를 바탕으로 `topScore`와 `scoreMargin`을 계산한다.

## Provider 추상화

```text
LlmProvider
├─ LocalLlmProvider
└─ CloudLlmProvider

EmbeddingProvider
├─ LocalEmbeddingProvider
└─ CloudEmbeddingProvider

StoragePort
├─ R2StorageAdapter
├─ S3StorageAdapter
└─ MinioStorageAdapter
```

provider 선택은 고객사별 배포 설정으로 결정하며 관리자 화면에서 변경하지 않는다.
모든 구현체는 동일한 요청/응답 계약, timeout, 오류 타입을 제공해야 한다.

## 레포 책임

### AI

- 사용자에게 반환하는 최종 LLM 응답 마스킹
- 문서 chunking, embedding, Qdrant retrieval
- Cross-Encoder 기반 문서·부서 후보 reranking
- 출처 기반 답변 생성과 출처 검증
- 활성·승인 Tool 선택, 입력 검증과 결과 기반 답변 생성
- 매뉴얼 → 워키 → 지식 RAG → Tool 순차 폴백 오케스트레이션
- 부서 R&R·승인 사례 기반 부서 후보 검색
- 수기 지식·승인 지식·라우팅 사례의 Qdrant 동기화

### BE

- 인증, 사용자 권한과 감사 로그
- 챗봇 세션·메시지, 티켓과 관리자 설정 저장
- API Tool 정의와 credential reference 관리
- DB Query Tool 템플릿 검증·승인 상태 관리
- Tool HTTP/DB 실행과 감사 로그
- 수기 지식·승인 지식 CRUD와 `ai_sync_jobs` 기반 동기화 작업 저장
- 최종 부서 배정과 라우팅 결과 저장
- R2/S3/MinIO Object Storage 추상화

## BE에서 AI 호출

- AI 주소는 `AI_BASE_URL`로 설정하며 배포 환경에서는 컨테이너 간 접근 가능한 주소를 사용한다.
- 라우팅·지식 동기화와 챗봇은 timeout이 다르므로 별도 `RestClient`로 구성한다.
- 티켓 라우팅 실패는 `COMMON_QUEUE`로 fallback한다.
- R&R 편집·지식 동기화는 RDB 변경과 `ai_sync_jobs` 생성을 같은 트랜잭션으로 처리하고, 커밋 이후 워커가 AI 서버를 호출한다.
- AI 요청·응답 DTO는 AI API의 camelCase 계약을 그대로 따른다.
- 챗봇 요청에는 현재 질문, 활성 `customPrompt`, 최근 `USER`/`ASSISTANT` 세션 메시지를 전달한다.
- AI 출처의 `sourceType`, `sourceId`, `chunkIndex`로 BE 인용 대상을 식별하고 인용 이력은 BE가 저장한다.
- 문서 인덱싱 응답은 AI 서버의 snake_case 계약(`source_id`, `indexed_chunks`)을 그대로 받아 BE에서 필요한 필드만 매핑한다.

## AI 동기화 작업

RDB 변경과 Qdrant 인덱싱 사이의 유실을 막기 위해 BE는 `ai_sync_jobs`를 outbox처럼 사용한다.
도메인 서비스는 기존 AI HTTP 즉시 호출 대신 같은 `@Transactional` 안에서 `PENDING` 작업을 저장한다.
별도 `@Scheduled` 워커가 커밋 이후 작업을 선점하고 AI 서버 호출 결과에 따라 `SYNCED` 또는 `FAILED`로 전이한다.
문서 인덱싱이 성공하면 AI 응답의 `indexed_chunks`를 `ai_sync_jobs.indexed_chunks`에 저장해 운영자가 실제 인덱싱 청크 수를 확인할 수 있게 한다. 응답이 없거나 청크 수를 제공하지 않는 동기화 경로는 `NULL`을 허용한다.

| sourceType | 대상 | AI 엔드포인트 |
|---|---|---|
| `MANUAL` | 파일/직접입력 매뉴얼 | `POST /api/v1/documents/ingest`, `POST /api/v1/documents/ingest-text`, `DELETE /api/v1/documents/{source_id}` |
| `WORKI` | 질문과 답변을 합친 워키 문서 | `POST /api/v1/documents/ingest-text`, `DELETE /api/v1/documents/{source_id}` |
| `KNOWLEDGE_DATA` | TEAM_ADMIN 승인 지식 | `POST /api/v1/documents/ingest-text`, `DELETE /api/v1/documents/{source_id}` |
| `MANUAL_KNOWLEDGE` | SYSTEM_ADMIN 수기 지식 | `POST /api/v1/documents/ingest-text`, `DELETE /api/v1/documents/{source_id}` |
| `DEPT_RR` | 부서 R&R 라우팅 지식 | `POST /api/v1/knowledge/sync`, `DELETE /api/v1/knowledge/{source_id}` |

워커는 `MANUAL` 전용 문서 워커와 텍스트 계열 워커로 나뉜다. 두 워커 모두 `FOR UPDATE SKIP LOCKED`로 `PENDING` 작업을 오래된 순서대로 선점하고, `lease_expires_at`이 지난 `PROCESSING` 작업은 재처리 대상으로 복구한다. 동일 source에 더 최신 `SYNCED` 작업이 있으면 오래된 작업은 AI 호출 없이 완료 처리한다.

재시도는 최대 5회이며 실패할 때마다 `retry_count`를 증가시키고 `retry_count^2`분 뒤 실행되도록 `next_retry_at`을 설정한다. 최대 재시도 초과 작업은 `FAILED`로 고정해 운영자가 확인할 수 있게 한다.

## 저장소 경계

- MariaDB: 업무 데이터와 AI 설정의 정본
- Elasticsearch: BE 전문 검색과 검색 통계
- Qdrant: AI RAG와 부서 라우팅 Vector Store
- Redis: 임시 데이터와 Flash Chat TTL
- Object Storage: 첨부 이미지와 매뉴얼 원본 파일
