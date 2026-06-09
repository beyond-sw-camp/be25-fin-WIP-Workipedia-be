# ADR 007 - Notification Strategy

> 문서 유형: ADR
> 상태: Draft
> 정본 위치: `docs/adr/007-notification-strategy.md`
> 관련 문서: `docs/reference/prd.md`, `docs/reference/service-flow.md`, `docs/api/api-contract.md`
> 버전: v0.1
> 최종 수정: 2026-05-31

## Context

Workipedia에는 질문 답변, 티켓 배정, 티켓 상태 변경, 답변 채택, 지식화 승인 등 사용자가 놓치면 안 되는 이벤트가 있다.

다만 MVP 일정상 WebSocket/SSE 같은 실시간 인프라를 처음부터 안정적으로 운영하기는 부담이 있다.

## Decision

MVP 알림은 **DB 저장 + 조회 API 기반 알림함**으로 시작한다.

기본 원칙:

- 이벤트 발생 시 `notifications`에 알림 레코드를 저장한다.
- 프론트는 알림 목록 API를 호출해 읽지 않은 알림을 표시한다.
- 알림 클릭 시 관련 리소스인 워키, 티켓, 챗봇 세션, 관리자 화면으로 이동한다.
- 실시간 push는 후순위로 둔다.
- 데모에서는 일정 주기 polling 또는 수동 새로고침으로도 충분하다.

주요 알림 이벤트:

- 본인 질문에 답변 등록
- 본인 답변 채택
- 본인 요청 티켓 상태 변경
- 팀에 신규 티켓 배정
- 팀원이 티켓 담당자로 배정됨
- 티켓이 공통 접수 큐로 이동
- 지식화 승인/동기화 실패

## Consequences

- MVP 구현 난이도를 낮출 수 있다.
- 알림 이력이 DB에 남아 디버깅과 시연이 쉽다.
- 실시간성은 약하지만, 발표 핵심 흐름에는 충분하다.
- 이후 WebSocket/SSE를 붙이더라도 알림 저장 구조는 그대로 재사용할 수 있다.

## Discussion Needed

- polling 주기를 둘지, 화면 진입 시 조회만 할지 결정이 필요하다.
- 알림 삭제를 허용할지, 읽음 처리만 허용할지 결정이 필요하다.
- 팀 알림을 `TEAM_ADMIN`에게만 보낼지, 부서원 전체에게 보낼지 결정이 필요하다.
- 알림 종류별 이동 URL 규칙을 API 계약에 반영해야 한다.
- 실시간 알림을 발표 전 MVP에 포함할지 후순위로 둘지 최종 결정이 필요하다.
