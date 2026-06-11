# ADR 002 - RAG and AI Orchestration Strategy

> 문서 유형: ADR
> 상태: Accepted
> 정본 위치: `docs/adr/002-rag-strategy.md`
> 관련 문서: `docs/reference/trd.md`, `docs/adr/008-local-llm-security-strategy.md`, `docs/dev/domain-guides/chatbot-rag.md`
> 버전: v0.3
> 최종 수정: 2026-06-09

## Context

Workipedia의 AI는 사내 매뉴얼, 워키, 승인 지식과 해결된 티켓 이력을 근거로 답변하고 업무 요청을 담당 부서로 연결해야 한다. 지식 최신성과 출처 추적이 핵심이므로 모델 자체에 지식을 학습시키는 방식보다 검색 근거와 실패 처리를 명확히 관리할 수 있는 구조가 필요하다.

## Decision

- 지식 제공은 RAG로 통일하며 QLoRA와 LangGraph는 사용하지 않는다.
- AI Vector Store는 ChromaDB를 사용한다. BE의 Elasticsearch는 전문 검색과 BE 검색 기능을 담당하며 서로 대체하지 않는다.
- 검색 후보는 Cross-Encoder로 재정렬한다. 반환 계약에는 `candidate_id`, 원본 `score`, `rank`를 포함한다.
- 운영 응답에 mock 답변을 사용하지 않는다.
- 고객사별 배포에서 LLM과 Embedding provider를 로컬 또는 클라우드 구현체로 선택한다.

### Fallback pipeline

폴백 순서: A 매뉴얼 → B 워키 → C 지식화 게시판 → D Tool Calling → E 수기 지식

```text
A. 매뉴얼 RAG
→ NO_RESULT 또는 ERROR
B. 워키 RAG
→ NO_RESULT 또는 ERROR
C. TEAM_ADMIN 승인 지식화 게시판 RAG
→ NO_RESULT 또는 ERROR
D. 등록된 Tool 호출
→ NO_RESULT 또는 ERROR
E. SYSTEM_ADMIN 수기 지식 RAG
→ NO_RESULT 또는 ERROR
요청 티켓 생성 전환 액션
```

해결된 티켓 이력은 별도 단계가 아니며 TEAM_ADMIN 승인 지식화 게시판(c)으로만 검색에 반영한다.

오케스트레이션은 명시적인 Python `for` loop와 `if-else`로 구현한다. 각 단계는 답변 문자열이 아니라 다음 상태 중 하나를 반환한다.

| 상태 | 의미 |
|---|---|
| `SUCCESS` | 유효한 근거나 Tool 결과로 답변 완료 |
| `NO_RESULT` | 근거 부족으로 다음 단계 진행 |
| `ERROR` | timeout 등 실행 실패로 다음 단계 진행 |
| `BLOCKED` | 보안 또는 입력 검증 실패로 즉시 안전 응답 |

LLM 응답에서 특정 문구를 찾아 fallback 여부를 판단하지 않는다.

### Negative answer

다음 중 하나면 `NO_RESULT`로 처리한다.

- 검색된 chunk가 없음
- Cross-Encoder 최고 점수가 설정 임계값 미만
- 유효한 출처가 없음
- 생성 답변의 인용 ID가 검색 결과와 일치하지 않음
- 구조화된 생성 결과가 `INSUFFICIENT_CONTEXT`를 반환

민감정보 마스킹 실패나 허용되지 않은 Tool 입력은 `BLOCKED`로 처리한다.

### Prompt policy

```text
final_system_prompt = base_prompt + custom_prompt
```

- `base_prompt`: 코드 또는 배포 설정으로 관리하는 보안·행동 기준. 관리자 화면에서 수정하지 않는다.
- `custom_prompt`: SYSTEM_ADMIN이 관리하는 고객사 운영 지침. 활성 상태와 변경 이력을 저장한다.

## Consequences

- 지식 변경은 재학습 없이 chunking, embedding, upsert로 반영한다.
- 검색 실패와 보안 차단을 구조화된 상태로 감사할 수 있다.
- 고객사별 로컬/클라우드 차이는 동일 provider 계약으로 격리한다.
- 임계값은 고정 숫자가 아니라 평가 데이터로 보정해야 한다.

## Open Questions

- 문서 유형별 chunk 크기와 overlap
- Cross-Encoder 점수 정규화와 `NO_RESULT` 임계값
- 고객사별 LLM/Embedding provider 성능 기준
