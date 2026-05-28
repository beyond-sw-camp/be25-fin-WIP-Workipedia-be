# ADR 0003 - Auth Strategy

> 문서 유형: ADR
> 상태: Draft
> 정본 위치: `docs/adr/0003-auth-strategy.md`
> 관련 문서: `docs/reference/prd.md`, `docs/reference/trd.md`, `docs/api/api-contract.md`
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
- 권한은 `USER`, `ADMIN` 두 단계
- 비활성 사용자 로그인 차단
- 관리자 API는 `ADMIN`만 접근

## Consequences

- MVP 구현 속도가 빠르다.
- 프론트/백엔드 API 계약이 단순하다.
- 추후 SSO 연동 시 migration 계획이 필요하다.

## Open Questions

- Refresh Token 저장 위치를 DB/Redis 중 어디로 할지 결정 필요.
- Access Token을 프론트에서 어디에 저장할지 결정 필요.
