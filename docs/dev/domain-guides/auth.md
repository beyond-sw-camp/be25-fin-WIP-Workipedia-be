# Auth Domain Guide

> 문서 유형: Development Guide
> 상태: Draft
> 정본 위치: `docs/dev/domain-guides/auth.md`
> 관련 문서: `docs/adr/003-auth-strategy.md`, `docs/api/api-contract.md`, `docs/reference/prd.md`
> 버전: v0.1
> 최종 수정: 2026-06-03

## 개발 목표

사용자가 사번/비밀번호로 로그인하고, role과 department 기준으로 API 접근 권한을 분리한다.

## 먼저 볼 문서

- `docs/adr/003-auth-strategy.md`
- `docs/adr/005-role-permission-strategy.md`
- `docs/api/api-contract.md`
- `docs/reference/prd.md`

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

## 회원가입 구현 범위

회원가입 화면에서 호출되는 API는 아래 4개다.

| 순서 | API | 역할 |
|---|---|---|
| 1 | `GET /api/v1/departments` | 회원가입 화면의 부서 선택 목록 조회 |
| 2 | `POST /api/v1/auth/signup/code` | 회원가입 인증코드 발송 |
| 3 | `POST /api/v1/auth/signup/code/verify` | 회원가입 인증코드 확인 |
| 4 | `POST /api/v1/auth/signup` | 최종 회원가입 처리 |

### 부서 선택 흐름

- 사용자는 화면에서 부서명을 선택한다.
- 프론트는 `GET /api/v1/departments` 응답의 `departmentName`을 화면에 표시한다.
- 사용자가 부서를 선택하면 프론트는 해당 부서의 `departmentId`를 보관한다.
- 최종 회원가입 요청에는 부서명이 아니라 `departmentId`를 전달한다.

### 최종 회원가입 처리

`POST /api/v1/auth/signup`은 아래 순서로 처리한다.

1. Redis에서 해당 이메일의 인증 완료 상태를 확인한다.
2. 인증 완료 상태가 없으면 회원가입을 거부한다.
3. `departmentId`로 부서를 조회한다.
4. 사번 중복 여부를 확인한다.
5. 이메일 중복 여부를 확인한다.
6. 비밀번호를 BCrypt로 암호화한다.
7. 서버에서 자동 닉네임을 생성한다.
8. `users` 테이블에 사용자 정보를 저장한다.

`passwordConfirm`은 서버 Request Body에 포함하지 않는다.
비밀번호 확인값은 프론트에서 `password`와 일치하는지 검증한다.

### 자동 닉네임 정책

- 사용자는 회원가입 시 닉네임을 입력하지 않는다.
- 서버가 회원가입 완료 시점에 랜덤 닉네임을 자동 생성한다.
- 닉네임은 `NICKNAME_PREFIXES + NICKNAME_SUFFIXES` 조합으로 만든다.
- 닉네임 뒤에 숫자는 붙이지 않는다.
- 닉네임 중복은 허용한다.

예시:

```text
연결하는전략가
성장하는멘토
공유하는조력자
개선하는아키텍트
```

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

1. `POST /api/v1/auth/signup/code`
   - 이메일을 받아 인증코드를 생성한다.
   - 생성한 인증코드를 Redis에 저장한다.
   - 로컬 환경에서는 인증코드를 콘솔 로그에 출력한다.
   - 운영 환경에서는 인증코드를 이메일로 발송한다.
2. `POST /api/v1/auth/signup/code/verify`
   - 이메일과 인증코드를 받는다.
   - 사용자가 입력한 인증번호가 Redis에 저장된 인증번호와 일치하는지 확인한다.
   - 일치하면 회원가입 인증 완료 상태를 Redis에 저장한다.
3. `POST /api/v1/auth/signup`
   - 회원가입 전에 해당 이메일의 인증 완료 상태를 확인한다.
   - 인증 완료 상태가 없으면 회원가입을 거부한다.

### 로컬 Postman 테스트

로컬 기본 발송 방식은 `console`이다.
별도 설정이 없으면 `APP_MAIL_SENDER=console`과 동일하게 동작한다.

1. `POST /api/v1/auth/signup/code`를 호출한다.
2. 서버 콘솔 로그에서 인증코드를 확인한다.

```text
[회원가입 인증코드] email=user@company.com, code=123456
```

3. 콘솔에 출력된 인증코드를 `POST /api/v1/auth/signup/code/verify` 요청에 사용한다.
4. 인증 확인이 완료되면 `POST /api/v1/auth/signup`을 호출한다.

Postman 응답 Body에는 인증코드를 포함하지 않는다.
인증코드는 로컬 서버 콘솔에서만 확인한다.

### 운영 SMTP 전환

운영 환경에서는 콘솔 발송을 사용하지 않는다.
아래 설정을 통해 SMTP 발송 구현체를 활성화한다.

```properties
APP_MAIL_SENDER=smtp
```

추가로 `spring.mail.*` SMTP 설정이 필요하다.
SMTP 서버는 고객사 또는 배포 환경에 맞게 설정으로 주입하며, Gmail 등 특정 메일 서비스를 코드에 고정하지 않는다.

예시:

```yaml
spring:
  mail:
    host: ${MAIL_HOST}
    port: ${MAIL_PORT}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
```

### 비밀번호 재설정 인증 흐름

1. `POST /api/v1/auth/password-reset/code`
   - 이메일을 받아 인증코드를 생성한다.
   - 생성한 인증코드를 Redis에 저장한다.
   - 인증코드를 이메일로 발송한다.
2. `POST /api/v1/auth/password-reset/code/verify`
   - 이메일과 인증코드를 받는다.
   - 사용자가 입력한 인증번호가 Redis에 저장된 인증번호와 일치하는지 확인한다.
   - 일치하면 비밀번호 재설정 인증 완료 상태를 Redis에 저장한다.
3. `PATCH /api/v1/auth/password-reset`
   - 비밀번호 변경 전에 해당 이메일의 인증 완료 상태를 확인한다.
   - 인증 완료 상태가 없으면 비밀번호 재설정을 거부한다.

## 완료 기준

- 회원가입 화면에서 부서 목록을 조회할 수 있다.
- 인증코드는 숫자 6자리로 생성된다.
- 로컬 환경에서 인증코드는 서버 콘솔 로그로 확인할 수 있다.
- 인증코드 확인 성공 시 Redis에 회원가입 인증 완료 상태가 저장된다.
- 인증 완료되지 않은 이메일로 회원가입하면 실패한다.
- 사번 또는 이메일이 중복되면 회원가입이 실패한다.
- 비밀번호는 BCrypt로 암호화되어 저장된다.
- 닉네임은 서버에서 자동 생성된다.
- 로그인 성공 시 access token과 사용자 기본 정보가 반환된다.
- 잘못된 비밀번호는 로그인 실패한다.
- 비활성 사용자는 로그인할 수 없다.
- 권한이 없는 API 접근은 거부된다.

## 에러 처리 정책

Auth 도메인도 별도 ExceptionHandler나 ErrorResponse를 만들지 않는다.
공통 구조인 `CustomException + ErrorType + GlobalExceptionHandler`를 사용한다.

회원가입 관련 에러 코드는 `ErrorType`에 정의한다.

| ErrorType | status | HTTP | 의미 |
|---|---|---|---|
| `AUTH_EMAIL_VERIFICATION_REQUIRED` | `auth-001` | 400 | 이메일 인증 완료 상태가 없음 |
| `AUTH_EMAIL_CODE_MISMATCH` | `auth-002` | 400 | 인증코드 불일치 또는 만료 |
| `AUTH_DUPLICATE_EMAIL` | `auth-003` | 409 | 이미 사용 중인 이메일 |
| `AUTH_DUPLICATE_EMPLOYEE_ID` | `auth-004` | 409 | 이미 사용 중인 사번 |
| `AUTH_DEPARTMENT_NOT_FOUND` | `auth-005` | 404 | 부서를 찾을 수 없음 |
| `AUTH_EMAIL_SEND_FAILED` | `auth-006` | 500 | 인증코드 이메일 발송 실패 |

## 논의 필요 사항

- refresh token 저장 위치: Redis 또는 DB
- access token 저장 위치: 메모리 또는 cookie
- local 테스트에서 secure cookie를 어떻게 처리할지
- 운영 SMTP 서버와 계정 정책 확정 필요
