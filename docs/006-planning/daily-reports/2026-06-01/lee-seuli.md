# Daily Report — 이슬이 2026-06-01

> 문서 유형: Daily Report
> 상태: Draft
> 정본 위치: `docs/006-planning/daily-reports/2026-06-01/lee-seuli.md`
> 관련 문서: `docs/006-planning/daily-plans/2026-06-01.md`, `docs/006-planning/weekly-wbs/2026-06-01-week1.md`, `docs/006-planning/member-wbs/lee-seuli.md`
> 버전: v0.1
> 최종 수정: 2026-06-01

## 완료

- 회원가입 API 기본 구조를 추가했다.
  - `POST /api/v1/auth/signup`
  - `SignupRequest`, `SignupResponse`
  - `AuthController`, `AuthService`
- 회원가입 요청 검증 조건을 정리했다.
  - 사번, 부서, 이메일, 비밀번호 필수 입력 검증
  - 이메일 형식 검증
  - 비밀번호 영문자/숫자 조합 8~16자 검증
  - 검증 실패 메시지 추가
- 회원가입 저장 로직 구현을 위한 사용자/부서 도메인 기반을 추가했다.
  - `User`, `UserRole`, `UserStatus`, `UserRepository`
  - `Department`, `DepartmentRepository`
- `dev` 브랜치 최신 변경사항을 `feat/auth-signup` 브랜치에 병합하고 원격 브랜치에 push했다.

## 미완료

- `AuthService.signup()` 실제 회원가입 로직 구현이 남아 있다.
  - 부서 존재 여부 확인
  - 사번/이메일 중복 검사
  - 비밀번호 BCrypt 암호화
  - 닉네임 생성
  - 사용자 저장 및 응답 반환
- 회원가입 API 수동 확인 및 테스트 작성이 남아 있다.
- JPA/DataSource/Flyway 자동설정 제외 상태를 확인해야 한다.

## 다음 근무일 논의

- 회원가입 시 닉네임 생성 정책을 확정해야 한다.
- 회원가입/로그인 API를 인증 없이 접근 가능하게 하는 Security 설정 위치를 정해야 한다.
- Validation 실패 시 개별 필드 메시지를 응답에 내려줄지 공통 `BAD_REQUEST` 메시지만 사용할지 논의가 필요하다.
- `role`, `status`를 Java enum으로 유지하고 DB에는 `VARCHAR`로 저장하는 방식에 대해 팀 컨벤션 확인이 필요하다.

## API/DB/화면 영향

- 인증이 필요한 API는 아래와 같이 Request Header에 JWT Access Token을 포함한다.

Request Header:

```http
Authorization: Bearer <accessToken>
```

- `<accessToken>`에는 로그인 응답으로 받은 Access Token을 넣는다.
- 권한(`USER`, `TEAM_ADMIN`, `SYSTEM_ADMIN`)은 별도 Header로 받지 않고, 서버가 Access Token 검증 후 사용자 정보를 기준으로 판단한다.
- `users`, `departments` 테이블을 사용하는 JPA 도메인/Repository가 추가되었다.
- DB schema 자체 변경은 없으므로 신규 migration은 추가하지 않았다.
- 비밀번호 정책은 영문자와 숫자를 포함한 8~16자이며, 현재 특수문자는 허용하지 않는다.

## 관련 링크

- Branch: `feat/auth-signup`
- Commit: `3cc346d feat: 회원가입 API 기본 구조 추가`
- Commit: `9f53600 refactor: 회원가입 요청 검증과 변수명 개선`
- Commit: `fd7d9ac feat: 사용자와 부서 도메인 모델 추가`
