# ADR 004 - Department Ticket Routing Strategy

> 문서 유형: ADR
> 상태: Accepted
> 정본 위치: `docs/adr/004-ticket-routing-strategy.md`
> 관련 문서: `docs/reference/service-flow.md`, `docs/dev/domain-guides/ticket-routing-ai.md`, `docs/api/api-contract.md`
> 버전: v0.2
> 최종 수정: 2026-06-09

## Context

요청 티켓은 담당 부서로 연결되어야 하지만 AI가 개인 담당자를 직접 배정하면 조직 운영과 권한 경계를 침범한다. 또한 최초 추천 결과만 누적하면 잘못된 추천이 반복될 수 있다.

## Decision

- AI는 부서까지만 추천하고 개인 담당자는 TEAM_ADMIN이 배정한다.
- SYSTEM_ADMIN이 작성한 부서 R&R과 TEAM_ADMIN이 승인한 최종 처리 사례를 검색 근거로 사용한다.
- Vector Search로 부서 후보 Top 3를 찾고 Cross-Encoder로 재정렬한다.
- 1위 점수와 1·2위 점수 차이가 기준을 통과하면 담당 부서 큐로 배정한다.
- 기준 미달이면 후보와 점수를 기록하고 `COMMON_QUEUE`로 보낸다.
- 최종 처리 완료와 TEAM_ADMIN 승인이 확인된 사례만 해당 부서의 라우팅 사례로 반영한다.
- 모델 온라인 학습이나 부서 벡터 직접 이동은 하지 않는다.
- 부서 R&R 설정은 시스템 구동의 필수 조건이 아니다. 미설정 부서는 승인 사례와 공통 접수 큐로 처리할 수 있지만 cold start 품질이 낮아진다.

Reranker 반환 계약:

```json
{
  "recommendedDepartmentId": 2,
  "topScore": 5.14,
  "scoreMargin": 1.27,
  "candidates": [
    {
      "candidateId": "department-2",
      "departmentId": 2,
      "score": 5.14,
      "rank": 1
    }
  ]
}
```

점수 범위는 모델마다 다르므로 `0~1`로 가정하지 않는다.

## Consequences

- 한 번의 오배정이 영구 학습되지 않고 최종 승인 사례로 교정된다.
- 신규 부서는 R&R만으로 시작하고 사례가 쌓이면 검색 근거가 강화된다.
- BE는 최종 배정과 처리 부서, 후보 점수, 승인 이력을 저장해야 한다.

## Open Questions

- Cross-Encoder 모델과 점수 정규화 방식
- 1위 최소 점수와 1·2위 최소 점수 차이
- 승인 사례의 보관 수, 시간 감쇠와 중복 제거
