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

## 이메일 인증코드 정책

회원가입과 비밀번호 재설정은 모두 사내 이메일 기반 인증코드를 사용한다.
외부 API는 목적별로 분리하되, 내부 인증코드 생성/저장/검증 로직은 공통화할 수 있다.

### 저장소

- 인증코드는 Redis에 저장한다.
- 인증코드는 숫자 6자리로 생성한다.
- 인증코드는 임시 데이터이므로 TTL을 반드시 둔다.
- 인증 성공 후에는 인증 완료 상태도 Redis에 일정 시간 저장한다.
- 로컬 개발 환경에서는 인증코드를 콘솔 로그로 출력한다.
- 운영 환경에서는 SMTP 설정을 통해 실제 이메일을 발송한다.
- 이메일 발송 방식은 `app.mail.sender` 설정으로 선택한다. 기본값은 `console`이고, 운영에서는 `smtp`를 사용한다.

예시:

```text
signup:email-code:user@company.com = 123456
signup:email-verified:user@company.com = true
password-reset:email-code:user@company.com = 123456
password-reset:email-verified:user@company.com = true
```

### 회원가입 인증 흐름

1. `POST /auth/signup/code`
   - 이메일을 받아 인증코드를 생성한다.
   - 생성한 인증코드를 Redis에 저장한다.
   - 인증코드를 이메일로 발송한다.
2. `POST /auth/signup/code/verify`
   - 이메일과 인증코드를 받는다.
   - 사용자가 입력한 인증번호가 Redis에 저장된 인증번호와 일치하는지 확인한다.
   - 일치하면 회원가입 인증 완료 상태를 Redis에 저장한다.
3. `POST /auth/signup`
   - 회원가입 전에 해당 이메일의 인증 완료 상태를 확인한다.
   - 인증 완료 상태가 없으면 회원가입을 거부한다.

### 비밀번호 재설정 인증 흐름

1. `POST /auth/password-reset/code`
   - 이메일을 받아 인증코드를 생성한다.
   - 생성한 인증코드를 Redis에 저장한다.
   - 인증코드를 이메일로 발송한다.
2. `POST /auth/password-reset/code/verify`
   - 이메일과 인증코드를 받는다.
   - 사용자가 입력한 인증번호가 Redis에 저장된 인증번호와 일치하는지 확인한다.
   - 일치하면 비밀번호 재설정 인증 완료 상태를 Redis에 저장한다.
3. `PATCH /auth/password-reset`
   - 비밀번호 변경 전에 해당 이메일의 인증 완료 상태를 확인한다.
   - 인증 완료 상태가 없으면 비밀번호 재설정을 거부한다.

## 완료 기준

- 로그인 성공 시 access token과 사용자 기본 정보가 반환된다.
- 잘못된 비밀번호는 로그인 실패한다.
- 비활성 사용자는 로그인할 수 없다.
- 권한이 없는 API 접근은 거부된다.

## 논의 필요 사항

- refresh token 저장 위치: Redis 또는 DB
- access token 저장 위치: 메모리 또는 cookie
- local 테스트에서 secure cookie를 어떻게 처리할지
