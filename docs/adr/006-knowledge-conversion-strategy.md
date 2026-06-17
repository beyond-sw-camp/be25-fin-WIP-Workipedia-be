# ADR 006 - Knowledge Conversion Strategy

> 문서 유형: ADR
> 상태: Accepted
> 정본 위치: `docs/adr/006-knowledge-conversion-strategy.md`
> 관련 문서: `docs/reference/service-flow.md`, `docs/reference/prd.md`, `docs/api/api-contract.md`
> 버전: v0.3
> 최종 수정: 2026-06-17

## Context

처리 완료 티켓에는 반복 활용할 수 있는 업무 지식과 개인 상황·자산번호·계정 정보가 함께 포함될 수 있다. 원문을 그대로 RAG에 넣지 않고 민감정보를 제거한 승인 지식으로 전환해야 한다.

## Decision

```text
티켓 처리 완료
→ AI가 민감정보를 마스킹하고 일반 절차 초안 생성
→ TEAM_ADMIN 검수·승인
→ knowledge_data와 ai_sync_jobs 작업을 같은 트랜잭션으로 저장
→ 커밋 후 `@Scheduled` 워커가 AI 동기화 호출
→ AI가 chunking/embedding/Qdrant upsert
→ SYNCED 또는 FAILED
```

- `knowledge_candidates` 중간 테이블은 사용하지 않는다.
- 승인 결과는 V15의 `knowledge_data`에 저장한다.
- 승인된 일반 절차형 지식만 Vector Store에 반영하며 마스킹 전 원문은 AI 로그와 Vector Store에 보관하지 않는다.
- RDB 저장과 Vector Store 반영을 하나의 트랜잭션으로 묶지 않는다.
- 실패한 동기화는 상태와 사유를 남기고 재시도한다.
- 승인된 처리 사례는 별도 라우팅 사례로도 반영할 수 있다.
- Qdrant point ID는 동일 지식과 chunk 순서에서 항상 같은 deterministic UUID가 생성되도록 한다.
- 수정·삭제 동기화는 `source_type`과 `source_id` payload filter로 대상 point를 찾아 처리한다.

## Consequences

- TEAM_ADMIN 승인 전 데이터가 RAG 근거로 노출되지 않는다.
- RDB와 Qdrant 사이의 비동기 정합성 관리가 필요하다.
- `ai_sync_jobs`는 V41 migration 기준으로 작업 상태와 재시도 이력을 관리한다.

## Open Questions

- 문서 유형별 chunk 크기와 overlap
- dead-letter 또는 관리자 수동 재처리 화면
- 승인 지식 수정·삭제 시 Qdrant 보상 처리
