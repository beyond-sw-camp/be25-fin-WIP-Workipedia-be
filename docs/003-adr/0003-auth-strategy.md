# ADR 0003 - Auth Strategy

> 문서 유형: ADR
> 상태: Draft
> 정본 위치: `docs/003-adr/0003-auth-strategy.md`
> 관련 문서: `docs/001-reference/prd.md`, `docs/001-reference/trd.md`, `docs/004-api/api-contract.md`
> 버전: v0.1
> 최종 수정: 2026-05-28

## Context

PRD는 사번/부서명/회사 이메일/비밀번호 기반 회원가입과 사번 기반 로그인을 요구한다. TRD는 JWT 기반 인증을 제안한다.

SSO나 사내 인증 시스템 연동은 MVP 범위에서는 불확실하다.

## Decision

MVP는 자체 JWT 인증으로 진행한다.

기본 정책:

- 사번 기반 로그인
- 비밀번호는 BCrypt 저장
- 권한은 `USER`, `TEAM_ADMIN`, `SYSTEM_ADMIN` 세 단계
- 비활성 사용자 로그인 차단
- 팀 티켓/지식화 검수 API는 `TEAM_ADMIN`이 접근
- 공통 접수 큐/전체 운영 지표 API는 `SYSTEM_ADMIN`이 접근

## Consequences

- MVP 구현 속도가 빠르다.
- 팀 관리자와 전체 관리자의 책임이 분리되어 티켓 본문 접근 범위를 줄일 수 있다.
- 추후 SSO 연동 시 migration 계획이 필요하다.

## Open Questions

- Refresh Token 저장 위치를 DB/Redis 중 어디로 할지 결정 필요.
- Access Token을 프론트에서 어디에 저장할지 결정 필요.
