# Workipedia Project Structure Guide

> 기준 문서: PRD v0.1, TRD v0.1, Constitution v0.1
> 정본 위치: `docs/architecture/project-structure.md`
> 작성일: 2026-05-28

## 1. 추천 방향

Workipedia는 MVP 범위가 넓다. 인증, 챗봇 RAG, 워키, 티켓, 매뉴얼, 포인트, 알림, 관리자, ESG가 모두 포함되어 있다.

따라서 초기 구조는 **모듈러 모놀리스**로 시작한다.

- 배포 단위는 Spring Boot 애플리케이션 1개
- 코드 구조는 도메인별 패키지로 분리
- 외부 의존성인 LLM, Embedding, Vector Store는 adapter로 격리
- 배치 작업은 같은 repo 안에 두되 사용자 요청 흐름과 분리
- Phase 2 이후 트래픽이나 팀 규모가 커지면 일부 모듈만 서비스로 분리

## 2. 전체 아키텍처

```text
Frontend
  |
  v
Spring Boot API
  |
  +-- auth
  +-- user / department
  +-- chatbot
  +-- rag
  +-- manual
  +-- worki
  +-- ticket
  +-- point / badge
  +-- notification
  +-- admin
  +-- esg
  +-- batch
  |
  +-- RDB: MariaDB/MySQL or PostgreSQL
  +-- Redis
  +-- Vector Store
  +-- LLM API
  +-- Embedding API
```

## 3. 백엔드 패키지 구조

```text
src/main/java/com/wip/workipedia/
  WorkipediaApplication.java

  global/
    config/
    security/
    exception/
    response/
    validation/
    util/

  auth/
    controller/
    service/
    dto/
    domain/
    repository/

  user/
    controller/
    service/
    dto/
    domain/
    repository/

  department/
    controller/
    service/
    dto/
    domain/
    repository/

  chatbot/
    controller/
    service/
    dto/
    domain/
    repository/

  rag/
    application/
    retriever/
    generator/
    guardrail/
    adapter/
    dto/

  manual/
    controller/
    service/
    dto/
    domain/
    repository/

  worki/
    controller/
    service/
    dto/
    domain/
    repository/

  ticket/
    controller/
    service/
    dto/
    domain/
    repository/

  point/
    service/
    domain/
    repository/

  badge/
    service/
    domain/
    repository/

  notification/
    controller/
    service/
    dto/
    domain/
    repository/

  admin/
    controller/
    service/
    dto/
    domain/
    repository/

  esg/
    controller/
    service/
    dto/

  batch/
    embedding/
    statistics/
```

## 4. 도메인별 책임

| 모듈 | 책임 |
|---|---|
| `global` | 공통 응답, 예외, 보안 설정, JWT, 공통 유틸 |
| `auth` | 회원가입, 로그인, 토큰 발급, 비밀번호 검증 |
| `user` | 사용자 프로필, 활성/비활성, 닉네임 |
| `department` | 부서 마스터, 담당 부서 조회 |
| `chatbot` | 채팅 세션, 메시지 저장, 워키 전환 |
| `rag` | 검색, 프롬프트 구성, LLM 호출, 출처 검증, PII guardrail |
| `manual` | 매뉴얼 등록/수정/삭제, chunk 생성 대상 관리 |
| `worki` | 질문, 답변, 채택, 반응, 상태 전이 |
| `ticket` | 티켓 생성, 부서 배정, 상태 전이, 공식 답변 |
| `point` | 포인트 정책, 포인트 이력 |
| `badge` | 뱃지 부여 기준, 사용자 뱃지 |
| `notification` | 답변/채택/티켓 변경 알림 |
| `admin` | 관리자 대시보드, 관리자 작업 로그 |
| `esg` | ESG-S/G 지표 조회 |
| `batch` | 임베딩 갱신, 통계 집계 |

## 5. 가장 중요한 경계

### 5.1 `chatbot`과 `rag`는 분리한다

`chatbot`은 대화 세션과 메시지 저장을 담당한다.

`rag`는 검색, LLM 호출, 출처 검증, 개인정보 필터링을 담당한다.

```text
ChatbotService
  -> RagAnswerService
    -> PrivacyGuardrail
    -> Retriever
    -> LlmGenerator
    -> ReferenceValidator
  -> ChatbotMessageRepository
```

이렇게 나누면 나중에 LLM 벤더를 바꾸거나 RAG 품질 평가 하네스를 붙이기 쉽다.

### 5.2 `worki`와 `ticket`은 상태 전이 기준으로 연결한다

워키는 공개 Q&A이고, 티켓은 공식 처리 흐름이다.

```text
챗봇 실패
-> worki 질문 등록
-> 해결 안 됨 또는 공식 답변 필요
-> ticket 생성
-> 담당 부서 답변
-> worki/티켓 상태 반영
```

티켓은 `worki_questions.question_id`를 optional로 참조한다. 챗봇에서 바로 티켓이 생기는 케이스도 있기 때문이다.

### 5.3 관리자 기능은 각 도메인에 흩뜨리지 않는다

관리자 API는 `admin` 모듈에 모으되 실제 도메인 변경은 각 서비스에 위임한다.

```text
AdminWorkiController
  -> AdminAuditService
  -> WorkiModerationService
```

모든 관리자 작업은 `admin_logs` 기록을 먼저 설계한다.

### 5.4 외부 API는 adapter 뒤에 숨긴다

LLM, Embedding, Vector Store는 직접 서비스 코드에서 호출하지 않는다.

```text
rag/adapter/
  LlmClient.java
  OpenAiLlmClient.java
  EmbeddingClient.java
  OpenAiEmbeddingClient.java
  VectorSearchClient.java
```

이 구조가 헌법의 "외부 의존성 격리" 원칙과 맞다.

## 6. DB 마이그레이션 순서

초기 Flyway migration은 기능 개발 순서와 맞춰 나눈다.

```text
src/main/resources/db/migration/
  V1__create_departments_and_users.sql
  V2__create_manuals.sql
  V3__create_worki.sql
  V4__create_chatbot.sql
  V5__create_tickets.sql
  V6__create_points_badges_notifications.sql
  V7__create_chunks_and_references.sql
  V8__create_admin_logs.sql
  V9__create_esg_statistics.sql
```

처음부터 모든 컬럼을 완벽하게 넣으려 하기보다, 테이블 명세서의 P0/P1 컬럼부터 반영한다.

## 7. MVP 개발 순서

### Step 1: 기반

- 공통 응답 포맷
- 예외 처리
- Spring Security 기본 설정
- JWT 인증
- USER/ADMIN 권한
- Flyway 활성화

### Step 2: 사용자/부서

- 회원가입
- 로그인
- 내 정보 조회
- 관리자 계정 비활성화
- 부서 마스터

### Step 3: 워키

- 질문 등록/수정
- 답변 등록
- 답변 채택
- 반응
- 사용자 삭제 제한
- soft delete 기반 관리자 삭제

### Step 4: 매뉴얼

- 관리자 매뉴얼 등록/수정/삭제
- 매뉴얼 조회
- manual chunk 생성 준비

### Step 5: 챗봇/RAG mock

- 채팅 세션 생성
- 메시지 저장
- mock retriever + mock LLM
- 출처 포함 응답
- 답변 실패 시 워키 등록 payload 반환

### Step 6: 티켓

- 워키 질문 기반 티켓 생성
- 담당 부서 배정
- 상태 전이
- 부서 담당자 답변

### Step 7: 실제 RAG 연결

- Embedding adapter
- Vector Store adapter
- chunk indexing batch
- references 저장
- PII guardrail

### Step 8: 운영 기능

- 관리자 대시보드
- 포인트/뱃지
- 알림
- FAQ
- ESG 지표

## 8. 테스트 구조

```text
src/test/java/com/wip/workipedia/
  auth/
  worki/
  ticket/
  chatbot/
  rag/
  admin/
  harness/
```

초기 필수 테스트:

| 영역 | 테스트 |
|---|---|
| auth | 비밀번호 정책, 비활성 사용자 로그인 차단 |
| worki | WAITING 상태에서만 수정, USER 삭제 불가, 채택 후 추가 답변 제한 |
| chatbot | 출처 없는 답변 실패, 메시지 저장 |
| rag | PII 마스킹, 근거 부족 응답 |
| ticket | 상태 전이, 담당 부서 권한 |
| admin | admin_logs 기록 |
| harness | Golden Dataset 기반 RAG/정책 검증 |

## 9. 팀원 분업 추천

현재 팀 역할 기준으로 아래처럼 나눈다.

| 담당 | 범위 |
|---|---|
| 민정기 | worki, FAQ, notification, docs |
| 김가영 | admin, point, badge, ESG metrics |
| 김진혁 | ticket, ticket transfer, local RAG, chatbot answer flow, docs |
| 이슬이 | auth, user, security, chatbot sessions/messages |
| 황희수 | frontend |

단, 챗봇은 이슬이와 김진혁의 경계를 초반에 맞춘다. 이슬이는 세션/메시지 저장, 김진혁은 local RAG/출처/전환 정책을 담당한다.

## 10. 지금 결정하면 좋은 것

| 결정 | 추천 |
|---|---|
| DB | 현재 의존성 기준 MariaDB로 MVP 시작. Vector Store는 별도 adapter로 둔다 |
| Vector Store | local embedding 검색을 우선 구현하고, 저장소는 RDB 기반 최소 vector table 또는 local adapter로 시작한다 |
| 인증 | 자체 JWT 먼저. SSO는 Phase 3 |
| RAG | local embedding first, mock fallback. 출처 저장과 no-answer 전환을 필수로 둔다 |
| 배치 | Quartz로 일 1회 임베딩 job 시작 |
| 모듈 분리 | 패키지 모듈러 모놀리스. 멀티모듈 Gradle은 MVP 이후 |
