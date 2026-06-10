# ADR 005 - Role Permission Strategy

> 문서 유형: ADR
> 상태: Draft
> 정본 위치: `docs/adr/005-role-permission-strategy.md`
> 관련 문서: `docs/reference/constitution.md`, `docs/reference/prd.md`, `docs/reference/service-flow.md`, `docs/api/api-contract.md`
> 버전: v0.1
> 최종 수정: 2026-05-31

## Context

Workipedia는 사내 지식 검색뿐 아니라 실제 업무 요청 티켓을 다룬다.
따라서 모든 사용자가 모든 티켓을 볼 수 있으면 개인정보, 부서 업무, 운영 신뢰 문제가 생긴다.

초기에는 단순한 권한 구조로 시작하되, 사용자/팀 관리자/전체 관리자의 책임을 명확히 나눠야 한다.

## Decision

MVP 권한은 다음 3개 역할로 운영한다.

| Role | 책임 |
|---|---|
| `USER` | 질문 작성, 요청 티켓 발행, 본인 티켓 상태 확인, 워키 답변 작성 |
| `TEAM_ADMIN` | 자기 팀 티켓 확인, 팀원 배정, 티켓 이관 요청, 지식화 승인 |
| `SYSTEM_ADMIN` | 공통 접수 큐 관리, 자동 배정 실패 검토, 부서 R&R·AI Tool·수기 지식 관리, 운영 지표 확인 |

권한 원칙:

- `USER`는 본인이 생성한 요청 티켓만 조회한다.
- `TEAM_ADMIN`은 자기 팀에 배정된 티켓을 조회하고 팀원에게 분배한다.
- `TEAM_ADMIN`은 직접 다른 부서로 이관하지 않고 공통 접수 큐로 보낸다.
- `SYSTEM_ADMIN`은 공통 접수 큐와 운영 지표를 관리한다.
- AI는 담당 부서까지만 추천하며 개인 담당자 배정은 `TEAM_ADMIN`이 수행한다.
- DB Query Tool의 SQL과 접속정보는 개발자가 관리하고 `SYSTEM_ADMIN`은 승인된 Tool의 활성 상태만 변경한다.
- 전체 관리자 대시보드는 개인 평가가 아니라 운영 현황 파악을 목적으로 한다.
- 관리자 작업은 추적 가능해야 하며, 중요한 작업은 `admin_logs`에 기록한다.

## Consequences

- 티켓 본문 접근 범위를 줄여 운영 신뢰를 높일 수 있다.
- 팀 관리자는 자기 팀 요청 처리에 집중하고, 전체 관리자는 라우팅/운영 흐름에 집중한다.
- API는 role뿐 아니라 department membership을 함께 확인해야 한다.
- 프론트 화면은 role에 따라 메뉴와 액션을 다르게 보여줘야 한다.

## Discussion Needed

- `TEAM_ADMIN`이 팀원 개인별 처리 실적을 볼 수 있는 범위를 정해야 한다.
- `SYSTEM_ADMIN`이 전체 티켓 본문을 볼 수 있는지, 운영 메타데이터 중심으로 제한할지 결정이 필요하다.
- `admin_logs` 기록 대상 액션을 확정해야 한다.
- 한 사용자가 여러 부서 또는 여러 role을 가질 수 있는지 결정이 필요하다.
- 부서장과 팀 관리자를 같은 개념으로 볼지, 별도 직책 필드가 필요한지 결정이 필요하다.
