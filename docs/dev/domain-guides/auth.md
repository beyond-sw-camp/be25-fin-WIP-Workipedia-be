# Auth Domain Guide

> 문서 유형: Development Guide
> 상태: Draft
> 정본 위치: `docs/dev/domain-guides/auth.md`
> 관련 문서: `docs/adr/003-auth-strategy.md`, `docs/adr/005-role-permission-strategy.md`, `docs/api/api-contract.md`, `docs/reference/prd.md`
> 버전: v0.2
> 최종 수정: 2026-06-07

## 개발 목표

사용자가 사번과 비밀번호 기반으로 인증하고, JWT와 Redis를 사용해 로그인 상태를 관리한다.
회원가입, 로그인, 토큰 재발급, 로그아웃, 비밀번호 재설정 기능을 제공하며, 역할과 사용자 상태를 기준으로 인증/인가 흐름을 분리한다.

## 관련 문서

- `docs/adr/003-auth-strategy.md`
- `docs/adr/005-role-permission-strategy.md`
- `docs/api/api-contract.md`
- `docs/reference/prd.md`

## 구현 범위

| 기능 | API |
|---|---|
| 회원가입 부서 목록 조회 | `GET /api/v1/departments` |
| 회원가입 인증코드 발송 | `POST /api/v1/auth/signup/code` |
| 회원가입 인증코드 확인 | `POST /api/v1/auth/signup/code/verify` |
| 회원가입 | `POST /api/v1/auth/signup` |
| 로그인 | `POST /api/v1/auth/login` |
| 토큰 재발급 | `POST /api/v1/auth/token/refresh` |
| 로그아웃 | `POST /api/v1/auth/logout` |
| 비밀번호 재설정 인증코드 발송 | `POST /api/v1/auth/password-reset/code` |
| 비밀번호 재설정 인증코드 확인 | `POST /api/v1/auth/password-reset/code/verify` |
| 비밀번호 재설정 | `PATCH /api/v1/auth/password-reset` |
| 마이페이지 조회 | `GET /api/v1/me/profile` |
| 마이페이지 알림 설정 변경 | `PATCH /api/v1/me/notification-settings` |
| 내 발행 티켓 목록 조회 | `GET /api/v1/me/tickets` |
| 내 발행 티켓 상세 조회 | `GET /api/v1/me/tickets/{ticketId}` |

## 주요 구현 파일

| 파일 | 역할 |
|---|---|
| `AuthController` | Auth API 엔드포인트와 응답 Header/Body 구성 |
| `AuthService` | 회원가입, 로그인, 토큰 재발급, 로그아웃, 비밀번호 재설정 핵심 로직 |
| `SignupEmailCodeService` | 회원가입 인증코드 생성, 발송, 확인 흐름 |
| `PasswordResetEmailCodeService` | 비밀번호 재설정 인증코드 생성, 발송, 확인 흐름 |
| `EmailVerificationService` | 인증코드와 인증 완료 상태를 Redis에 저장/조회/삭제 |
| `RefreshTokenService` | Refresh Token을 Redis에 저장/검증/삭제 |
| `EmailSender` | 인증코드 이메일 발송 인터페이스 |
| `SmtpEmailSender` | SMTP 기반 실제 이메일 발송 구현체 |
| `JwtProvider` | Access Token, Refresh Token 생성 및 검증 |
| `JwtFilter` | `Authorization` Header의 Access Token을 검증하고 SecurityContext에 인증 정보 저장 |
| `JwtProperties` | JWT secret, Access Token 만료 시간, Refresh Token 만료 시간 설정 |
| `UserRepository` | 사번, 이메일, 사번+이메일 기준 사용자 조회 |

## 인증코드 정책

- 인증코드는 숫자 6자리로 생성한다.
- 인증코드는 Redis에 TTL과 함께 저장한다.
- 인증코드 확인 성공 시 인증 완료 상태를 Redis에 별도로 저장한다.
- 사용이 끝난 인증코드는 Redis에서 삭제한다.
- 현재 팀 정책상 콘솔 인증코드 발송은 사용하지 않는다.
- 인증코드는 SMTP 메일 발송 방식으로만 전달한다.

### Redis Key

| 용도 | Key 형식 |
|---|---|
| 회원가입 인증코드 | `signup:email-code:{email}` |
| 회원가입 인증 완료 상태 | `signup:email-verified:{email}` |
| 비밀번호 재설정 인증코드 | `password-reset:email-code:{employeeId}:{email}` |
| 비밀번호 재설정 인증 완료 상태 | `password-reset:email-verified:{employeeId}:{email}` |
| Refresh Token | `auth:refresh-token:{userId}` |

이메일은 앞뒤 공백 제거 후 소문자로 정규화하여 Redis Key에 사용한다.
비밀번호 재설정은 기존 사용자 확인이 필요하므로 `employeeId + email` 조합을 Key에 포함한다.

## 회원가입 흐름

### 1. 부서 목록 조회

`GET /api/v1/departments`

- 회원가입 화면에서 부서 선택 목록을 표시하기 위해 사용한다.
- 최종 회원가입 요청에는 부서명이 아니라 `departmentId`를 전달한다.

### 2. 회원가입 인증코드 발송

`POST /api/v1/auth/signup/code`

1. 이메일 형식을 검증한다.
2. 이미 가입된 이메일인지 확인한다.
3. 중복 이메일이면 `AUTH_DUPLICATE_EMAIL`을 반환한다.
4. 6자리 인증코드를 생성한다.
5. 인증코드를 이메일로 발송한다.
6. 인증코드를 Redis에 저장한다.

### 3. 회원가입 인증코드 확인

`POST /api/v1/auth/signup/code/verify`

1. 이메일과 인증코드 형식을 검증한다.
2. Redis에 저장된 인증코드와 사용자가 입력한 인증코드를 비교한다.
3. 일치하지 않으면 `AUTH_EMAIL_CODE_MISMATCH`를 반환한다.
4. 일치하면 회원가입 인증 완료 상태를 Redis에 저장한다.
5. 사용 완료된 인증코드는 Redis에서 삭제한다.

### 4. 회원가입

`POST /api/v1/auth/signup`

1. 이메일 인증 완료 상태를 Redis에서 확인한다.
2. 인증 완료 상태가 없으면 `AUTH_EMAIL_VERIFICATION_REQUIRED`를 반환한다.
3. `departmentId`로 부서를 조회한다.
4. 사번 중복 여부를 확인한다.
5. 이메일 중복 여부를 확인한다.
6. 비밀번호를 BCrypt로 암호화한다.
7. 서버에서 랜덤 닉네임을 생성한다.
8. `users` 테이블에 사용자 정보를 저장한다.

`passwordConfirm`은 프론트에서 확인용으로만 사용하며 서버 Request Body에는 포함하지 않는다.

## 로그인 흐름

`POST /api/v1/auth/login`

1. 사번으로 사용자를 조회한다.
2. 사용자가 없으면 `AUTH_INVALID_CREDENTIALS`를 반환한다.
3. 입력 비밀번호와 DB에 저장된 암호화 비밀번호를 비교한다.
4. 비밀번호가 일치하지 않으면 `AUTH_INVALID_CREDENTIALS`를 반환한다.
5. 사용자 상태가 `ACTIVE`인지 확인한다.
6. 비활성 사용자이면 `AUTH_INACTIVE_USER`를 반환한다.
7. Access Token과 Refresh Token을 발급한다.
8. Refresh Token을 Redis에 저장한다.
9. 마지막 로그인 시간을 갱신한다.
10. Access Token과 사용자 기본 정보는 Response Body로 반환한다.
11. Refresh Token은 `Set-Cookie` Header로 전달한다.

## JWT 정책

### Access Token

- Response Body로 반환한다.
- 일반 인증 API 호출 시 `Authorization: Bearer {accessToken}` Header에 담아 요청한다.
- `JwtFilter`가 Access Token을 검증하고 `SecurityContext`에 `userId`와 role을 저장한다.

### Refresh Token

- Response Body에 포함하지 않는다.
- HttpOnly Cookie로 전달한다.
- Redis에 `auth:refresh-token:{userId}` 형식으로 저장한다.
- 토큰 재발급과 로그아웃 처리에 사용한다.

### JWT Claims

| Claim | 의미 |
|---|---|
| `sub` | userId |
| `employeeId` | 사번 |
| `role` | 사용자 권한 |
| `type` | `access` 또는 `refresh` |
| `iat` | 발급 시각 |
| `exp` | 만료 시각 |

Access Token과 Refresh Token은 `type` Claim으로 구분한다.

## 토큰 재발급 흐름

`POST /api/v1/auth/token/refresh`

1. Refresh Token Cookie를 읽는다.
2. Refresh Token이 없으면 `AUTH_REFRESH_TOKEN_REQUIRED`를 반환한다.
3. JWT 서명, 만료 시간, 토큰 타입을 검증한다.
4. 유효하지 않으면 `AUTH_REFRESH_TOKEN_INVALID`를 반환한다.
5. 토큰에서 userId를 추출한다.
6. Redis에 저장된 Refresh Token과 요청 Refresh Token이 일치하는지 확인한다.
7. 일치하지 않으면 `AUTH_REFRESH_TOKEN_INVALID`를 반환한다.
8. 사용자를 조회하고 `ACTIVE` 상태인지 확인한다.
9. 새 Access Token과 새 Refresh Token을 발급한다.
10. 새 Refresh Token으로 Redis 값을 갱신한다.
11. 새 Access Token은 Response Body로 반환한다.
12. 새 Refresh Token은 `Set-Cookie` Header로 전달한다.

재발급 성공 시 Refresh Token을 새로 발급하므로 기존 Refresh Token은 Redis 검증에서 실패한다.

## 로그아웃 흐름

`POST /api/v1/auth/logout`

1. `Authorization` Header의 Access Token으로 사용자를 식별한다.
2. `JwtFilter`가 Access Token을 검증하고 `SecurityContext`에 userId를 저장한다.
3. Controller는 `@AuthenticationPrincipal Long userId`로 사용자 ID를 받는다.
4. Redis에 저장된 Refresh Token을 userId 기준으로 삭제한다.
5. `Set-Cookie` Header로 Refresh Token Cookie를 즉시 만료시킨다.

Access Token은 서버에 저장하지 않는 JWT이므로 로그아웃 시 직접 삭제하지 않는다.
대신 Refresh Token을 Redis와 Cookie에서 제거하여 추가 토큰 재발급을 막는다.

## 비밀번호 재설정 흐름

비밀번호 재설정은 로그인하지 못하는 사용자를 위한 기능이므로 Token 인증을 요구하지 않는다.
대신 `employeeId + email + 인증코드 확인 완료 상태`로 사용자를 검증한다.

### 1. 비밀번호 재설정 인증코드 발송

`POST /api/v1/auth/password-reset/code`

1. 사번과 이메일 형식을 검증한다.
2. `employeeId + email`이 DB에 저장된 사용자 정보와 일치하는지 확인한다.
3. 일치하는 사용자가 없으면 `AUTH_USER_NOT_FOUND`를 반환한다.
4. 6자리 인증코드를 생성한다.
5. 인증코드를 이메일로 발송한다.
6. 인증코드를 Redis에 저장한다.

### 2. 비밀번호 재설정 인증코드 확인

`POST /api/v1/auth/password-reset/code/verify`

1. 사번, 이메일, 인증코드 형식을 검증한다.
2. `employeeId + email`이 DB에 저장된 사용자 정보와 일치하는지 확인한다.
3. 일치하는 사용자가 없으면 `AUTH_USER_NOT_FOUND`를 반환한다.
4. Redis에 저장된 인증코드와 사용자가 입력한 인증코드를 비교한다.
5. 일치하지 않으면 `AUTH_EMAIL_CODE_MISMATCH`를 반환한다.
6. 일치하면 비밀번호 재설정 인증 완료 상태를 Redis에 저장한다.
7. 사용 완료된 인증코드는 Redis에서 삭제한다.

### 3. 비밀번호 재설정

`PATCH /api/v1/auth/password-reset`

1. 사번, 이메일, 새 비밀번호 형식을 검증한다.
2. Redis에서 비밀번호 재설정 인증 완료 상태를 확인한다.
3. 인증 완료 상태가 없으면 `AUTH_PASSWORD_RESET_VERIFICATION_REQUIRED`를 반환한다.
4. `employeeId + email`로 사용자를 조회한다.
5. 일치하는 사용자가 없으면 `AUTH_USER_NOT_FOUND`를 반환한다.
6. 새 비밀번호를 BCrypt로 암호화한다.
7. DB에 저장된 기존 비밀번호를 새 비밀번호로 변경한다.
8. Redis에 저장된 기존 Refresh Token을 삭제하여 기존 로그인 세션의 토큰 재발급을 차단한다.
9. Redis에 저장된 비밀번호 재설정 인증 완료 상태를 삭제한다.

`newPasswordConfirm`은 프론트에서 확인용으로만 사용하며 서버 Request Body에는 포함하지 않는다.

## 마이페이지 인증 API 흐름

마이페이지 관련 API는 모두 로그인한 사용자를 기준으로 동작한다.
사용자 식별은 Request Header의 Access Token을 `JwtFilter`가 검증한 뒤 `@AuthenticationPrincipal Long userId`로 전달하는 방식으로 처리한다.

### 1. 마이페이지 요약 조회

`GET /api/v1/me/profile`

1. Request Header의 Access Token으로 로그인 사용자를 식별한다.
2. 사용자 기본 정보, 발행 티켓 수, 포인트, ESG 점수, ESG 등급, 알림 설정 정보를 조회한다.
3. 마이페이지 첫 화면에 필요한 요약 정보를 한 번의 응답으로 반환한다.

### 2. 알림 설정 변경

`PATCH /api/v1/me/notification-settings`

1. Request Header의 Access Token으로 로그인 사용자를 식별한다.
2. 사용자의 전체 알림, 티켓 알림, Worki 알림, 매뉴얼 알림 설정값을 변경한다.
3. 상위 알림 설정을 끄면 하위 알림 설정도 모두 꺼진다.
4. 상위 알림 설정을 켜면 하위 알림 설정도 모두 켜진다.
5. 하위 알림 설정 3개가 모두 켜져 있으면 `allEnabled`는 `true`, 하나라도 꺼져 있으면 `false`로 처리한다.

### 3. 내 발행 티켓 목록 조회

`GET /api/v1/me/tickets`

1. Request Header의 Access Token으로 로그인 사용자를 식별한다.
2. 로그인한 사용자가 발행한 티켓 목록만 조회한다.
3. 기본 조회 상태는 `WAITING`이며, 화면의 "답변 대기" 탭에 해당한다.
4. "답변 완료" 탭을 조회할 때는 `status=COMPLETED`를 사용한다.
5. 외부 조회 상태 `WAITING`은 내부 티켓 상태 `RECEIVED`, `COMMON_QUEUE`, `ASSIGNED`, `IN_PROGRESS`를 포함한다.
6. 외부 조회 상태 `COMPLETED`는 내부 티켓 상태 `COMPLETED`를 의미한다.
7. 내부 티켓 상태 `REJECTED`, `DELETED`는 내 발행 티켓 목록에서 조회하지 않는다.
8. 각 티켓은 할당 부서 ID와 할당 부서명을 함께 반환한다.

### 4. 내 발행 티켓 상세 조회

`GET /api/v1/me/tickets/{ticketId}`

1. Request Header의 Access Token으로 로그인 사용자를 식별한다.
2. `ticketId`와 로그인 사용자 ID를 함께 조건으로 사용하여 본인이 발행한 티켓만 조회한다.
3. 삭제된 티켓은 조회 대상에서 제외한다.
4. 티켓 제목, 내용, 상태, 생성 시각, 할당 부서 ID, 할당 부서명을 반환한다.
5. 생성 시각 기준 48시간까지 남은 시간을 `remainingHours`로 계산한다.
6. 생성 후 48시간이 지났는지 여부를 `expired`로 반환한다.
7. 사용자가 발행한 티켓은 발행 이후 수정/삭제할 수 없으므로 `editable`, `deletable`은 항상 `false`로 반환한다.

## 메일 발송 정책

- 인증코드는 실제 이메일로 발송한다.
- `EmailSender` 인터페이스로 발송 방식을 분리한다.
- 현재 구현체는 `SmtpEmailSender`이다.
- SMTP 설정은 `spring.mail.*` 설정을 사용한다.
- 메일 발송 실패 시 `AUTH_EMAIL_SEND_FAILED`를 반환한다.

## 쿠키 정책

Refresh Token Cookie는 로그인, 토큰 재발급, 로그아웃에서 같은 정책을 사용한다.

| 속성 | 값 |
|---|---|
| 이름 | `refreshToken` |
| HttpOnly | `true` |
| Secure | `true` |
| SameSite | `Lax` |
| Path | `/api/v1/auth` |

로그인과 토큰 재발급 시에는 Refresh Token 값을 담아 발급한다.
로그아웃 시에는 빈 값과 `Max-Age=0`으로 쿠키를 만료시킨다.

## ErrorType

| ErrorType | status | HTTP | 의미 |
|---|---|---|---|
| `AUTH_EMAIL_VERIFICATION_REQUIRED` | `auth-001` | 400 | 회원가입 이메일 인증 완료 상태 없음 |
| `AUTH_EMAIL_CODE_MISMATCH` | `auth-002` | 400 | 인증코드 불일치 또는 만료 |
| `AUTH_DUPLICATE_EMAIL` | `auth-003` | 409 | 이미 사용 중인 이메일 |
| `AUTH_DUPLICATE_EMPLOYEE_ID` | `auth-004` | 409 | 이미 사용 중인 사번 |
| `AUTH_DEPARTMENT_NOT_FOUND` | `auth-005` | 404 | 존재하지 않는 부서 |
| `AUTH_EMAIL_SEND_FAILED` | `auth-006` | 500 | 인증코드 이메일 발송 실패 |
| `AUTH_INVALID_CREDENTIALS` | `auth-007` | 401 | 사번 또는 비밀번호 불일치 |
| `AUTH_INACTIVE_USER` | `auth-008` | 403 | 비활성화 사용자 |
| `AUTH_REFRESH_TOKEN_REQUIRED` | `auth-009` | 401 | Refresh Token 없음 |
| `AUTH_REFRESH_TOKEN_INVALID` | `auth-010` | 401 | 유효하지 않은 Refresh Token |
| `AUTH_USER_NOT_FOUND` | `auth-011` | 404 | 사번과 이메일에 일치하는 사용자 없음 |
| `AUTH_PASSWORD_RESET_VERIFICATION_REQUIRED` | `auth-012` | 400 | 비밀번호 재설정 인증 완료 상태 없음 |

## 완료 기준

- 회원가입 인증코드 발송/확인 API가 정상 동작한다.
- 회원가입 시 인증 완료 상태, 부서, 사번/이메일 중복 검증이 동작한다.
- 로그인 성공 시 Access Token Body와 Refresh Token Cookie가 발급된다.
- 토큰 재발급 시 Refresh Token Cookie와 Redis 저장값 검증이 동작한다.
- 토큰 재발급 성공 시 새 Access Token과 새 Refresh Token이 발급된다.
- 로그아웃 시 Redis Refresh Token이 삭제되고 Refresh Token Cookie가 만료된다.
- 비밀번호 재설정 인증코드 발송/확인이 정상 동작한다.
- 비밀번호 재설정 시 인증 완료 상태 확인 후 새 비밀번호가 암호화되어 저장된다.
- 비밀번호 재설정 완료 후 Redis Refresh Token이 삭제된다.
- 비밀번호 재설정 완료 후 Redis 인증 완료 상태가 삭제된다.
- Postman으로 주요 성공/실패 케이스를 확인한다.
