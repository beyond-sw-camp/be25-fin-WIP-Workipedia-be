# Workipedia AI Architecture Overview

> 문서 유형: Architecture Overview
> 상태: Draft
> 정본 위치: `docs/reference/ai-architecture-overview.md`
> 관련 문서: `docs/adr/002-rag-strategy.md`, `docs/adr/008-local-llm-security-strategy.md`, `docs/reference/trd.md`
> 최종 수정: 2026-06-11

## 핵심 원칙

- 지식 제공은 RAG로 통일한다.
- QLoRA와 LangGraph는 사용하지 않는다.
- 고객사별로 별도 배포하며 로컬/클라우드 차이는 provider 추상화와 배포 설정으로 처리한다.
- 민감정보 마스킹은 AI 서버가 전담하며 모델·Tool 호출 전과 Vector Store 저장 전에 수행한다. BE RDB에는 원문을 저장하되 Vector Store와 외부 LLM에는 마스킹된 텍스트만 전달한다.
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

민감정보 마스킹 실패나 허용되지 않은 Tool 입력은 `BLOCKED`로 처리한다.

## 주요 컴포넌트

```text
API Layer
└─ Orchestrator
   ├─ SensitiveDataMasker
   ├─ ManualRetriever
   ├─ WorkiRetriever
   ├─ KnowledgeRetriever
   │  ├─ KnowledgeDataRetriever
   │  └─ ManualKnowledgeRetriever
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

- 민감정보 탐지·마스킹
- 문서 chunking, embedding, ChromaDB retrieval
- Cross-Encoder 기반 문서·부서 후보 reranking
- 출처 기반 답변 생성과 출처 검증
- 활성·승인 Tool 선택, 결과 마스킹과 해석
- 매뉴얼 → 워키 → 지식 RAG → Tool 순차 폴백 오케스트레이션
- 부서 R&R·승인 사례 기반 부서 후보 검색
- 수기 지식·승인 지식·라우팅 사례의 ChromaDB 동기화

### BE

- 인증, 사용자 권한과 감사 로그
- 챗봇 세션·메시지, 티켓과 관리자 설정 저장
- API Tool 정의와 credential reference 관리
- DB Query Tool 템플릿 검증·승인 상태 관리
- Tool HTTP/DB 실행과 감사 로그
- 수기 지식·승인 지식 CRUD와 동기화 상태 저장
- 최종 부서 배정과 라우팅 결과 저장
- R2/S3/MinIO Object Storage 추상화

## 저장소 경계

- MariaDB: 업무 데이터와 AI 설정의 정본
- Elasticsearch: BE 전문 검색과 검색 통계
- ChromaDB: AI RAG와 부서 라우팅 Vector Store
- Redis: 임시 데이터와 Flash Chat TTL
- Object Storage: 첨부 이미지와 매뉴얼 원본 파일
