# Auth Domain Guide

> 문서 유형: Development Guide
> 상태: Draft
> 정본 위치: `docs/010-development/domain-guides/auth.md`
> 관련 문서: `docs/003-adr/003-auth-strategy.md`, `docs/004-api/api-contract.md`, `docs/001-reference/prd.md`
> 버전: v0.1
> 최종 수정: 2026-05-31

## 개발 목표

사용자가 사번/비밀번호로 로그인하고, role과 department 기준으로 API 접근 권한을 분리한다.

## 먼저 볼 문서

- `docs/003-adr/003-auth-strategy.md`
- `docs/003-adr/005-role-permission-strategy.md`
- `docs/004-api/api-contract.md`
- `docs/001-reference/prd.md`

## MVP 구현 범위

- 회원가입 또는 seed 사용자 생성
- 사번 기반 로그인
- BCrypt 비밀번호 저장
- JWT access token 발급
- refresh token 저장 방식 결정 반영
- `USER`, `TEAM_ADMIN`, `SYSTEM_ADMIN` 권한 분기
- 비활성 사용자 로그인 차단

## API/DB 영향

- `users`
- `departments`
- auth request/response DTO
- security filter
- role/department 기반 접근 제어

## 권한/보안 체크

- 비밀번호 plain text 저장 금지
- access token에 최소 claims만 포함
- refresh token을 프론트에서 어디에 저장할지 확정 필요
- 팀 티켓 API는 role뿐 아니라 department 확인 필요

## 완료 기준

- 로그인 성공 시 access token과 사용자 기본 정보가 반환된다.
- 잘못된 비밀번호는 로그인 실패한다.
- 비활성 사용자는 로그인할 수 없다.
- 권한이 없는 API 접근은 거부된다.

## 논의 필요 사항

- refresh token 저장 위치: Redis 또는 DB
- access token 저장 위치: 메모리 또는 cookie
- local 테스트에서 secure cookie를 어떻게 처리할지
