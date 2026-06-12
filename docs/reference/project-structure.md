# Workipedia Project Structure Guide

> 문서 유형: Architecture Guide
> 상태: Draft
> 정본 위치: `docs/reference/project-structure.md`
> 관련 문서: `docs/reference/constitution.md`, `docs/reference/service-flow.md`, `docs/reference/prd.md`, `docs/reference/trd.md`, `docs/adr/013-object-storage-strategy.md`
> 버전: v0.3
> 최종 수정: 2026-06-09

## 1. 추천 방향

Workipedia는 MVP 범위가 넓다. 인증, 챗봇 RAG, 워키, 티켓, 매뉴얼, 포인트, 알림, 관리자, ESG가 모두 포함되어 있다.

따라서 초기 구조는 **모듈러 모놀리스**로 시작한다.

- 배포 단위는 Spring Boot 애플리케이션 1개
- 코드 구조는 도메인별 패키지로 분리
- 외부 의존성인 LLM, Embedding, Vector Store는 adapter로 격리
- Object Storage는 `StoragePort`와 provider adapter로 격리
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
  +-- storage
  +-- point / esggrade
  +-- notification
  +-- admin
  +-- esg
  +-- batch
  |
  +-- RDB: MariaDB
  +-- Redis (Refresh Token)
  +-- Elasticsearch (Vector Store)
  +-- RabbitMQ (비동기 이벤트·작업 큐)
  +-- LLM API
  +-- Embedding API
  +-- Object Storage (R2 / S3 / MinIO)
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

  storage/
    controller/
    service/
    port/
    adapter/
    dto/

  point/
    service/
    domain/
    repository/

  esggrade/
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
| `chatbot` | 채팅 세션, 메시지 저장, 워키/요청 티켓 전환 |
| `rag` | 검색, 프롬프트 구성, LLM 호출, 출처 검증, PII guardrail |
| `manual` | 매뉴얼 등록/수정/삭제, chunk 생성 대상 관리 |
| `worki` | 질문, 답변, 채택, 반응, 상태 전이 |
| `ticket` | 요청 티켓 생성, 라우팅 점수, 자동 배정/공통 접수 큐, TEAM_ADMIN 이관 요청, 팀원 배정, 상태 전이, 공식 답변 |
| `storage` | presigned URL, 서버 직접 업로드, 삭제와 R2/S3/MinIO provider 격리 |
| `knowledge` | 처리 완료 티켓의 일반화 초안, TEAM_ADMIN 승인, Vector Store 동기화 상태 |
| `point` | 포인트 정책, 포인트 이력 |
| `esggrade` | ESG 점수 기준 등급, 사용자 현재 ESG 등급 |
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

### 5.2 `question`, `request`, `ticket`은 목적 기준으로 분리한다

질문은 기존 지식을 찾는 흐름이고, 요청은 실제 처리가 필요한 공식 티켓 흐름이다. 워키는 지식 축적 공간이고, 티켓은 담당 부서 처리 흐름이다.

```text
질문
-> 챗봇/RAG 검색
-> 매뉴얼 + 워키 출처 답변

요청
-> ticket 생성
-> 라우팅 신뢰도 산출
-> 자동 배정 또는 공통 접수 큐
-> TEAM_ADMIN 팀원 배정
-> 처리 완료
-> 지식화 초안 생성/승인
```

티켓은 `worki_questions.question_id`를 optional로 참조한다. 챗봇에서 바로 요청 티켓이 생기는 케이스도 있기 때문이다.

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

현재 DB는 초안 확정 전 단계이므로 초기 Flyway migration은 전체 스키마를 하나의 파일에 담는다.

```text
src/main/resources/db/migration/
  V1__create_initial_schema.sql
```

단, 이 `V1`이 팀원에게 공유되거나 PR에 올라가거나 dev에 merge된 뒤에는 수정하지 않는다.
공유 이후 스키마 변경은 `V2__...sql`, `V3__...sql`처럼 다음 번호 migration으로만 추가한다.

## 7. MVP 개발 순서

### Step 1: 기반

- 공통 응답 포맷
- 예외 처리
- Spring Security 기본 설정
- JWT 인증
- USER/TEAM_ADMIN/SYSTEM_ADMIN 권한
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

### Step 5: 챗봇/RAG

- 채팅 세션 생성
- 메시지 저장
- Qdrant retriever + 고객사별 LLM/Embedding provider
- Cross-Encoder reranking
- 출처 포함 응답
- `NO_RESULT` 시 매뉴얼→워키→지식 RAG→Tool 순서로 전환
- 지식 RAG는 분리 저장된 `KNOWLEDGE_DATA`와 `MANUAL_KNOWLEDGE` 후보를 함께 reranking

### Step 6: 티켓

- 질문/요청 기반 티켓 생성
- 라우팅 신뢰도 산출
- 자동 배정/공통 접수 큐
- TEAM_ADMIN 이관 요청 시 공통 접수 큐 이동
- TEAM_ADMIN 팀원 배정
- 상태 전이
- 부서 담당자 답변

### Step 7: 지식화

- 처리 완료 티켓의 지식화 생성/승인
- 개인 사례/개인정보 제거
- TEAM_ADMIN 검수
- Qdrant 비동기 동기화

### Step 8: AI 운영 연결

- LLM/Embedding provider adapter
- Tool Integration
- chunk indexing과 동기화 재시도
- references 저장
- PII guardrail

### Step 9: 운영 기능

- 관리자 대시보드
- 포인트/ESG 등급
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
| ticket | 상태 전이, 라우팅 점수, 공통 접수 큐, 담당 부서 권한 |
| knowledge | 처리 완료 티켓의 지식화 생성/승인과 동기화 |
| admin | admin_logs 기록 |
| harness | Golden Dataset 기반 RAG/정책 검증 |

## 9. 팀원 분업 추천

현재 팀 역할 기준으로 아래처럼 나눈다.

| 담당 | 범위 |
|---|---|
| 민정기 | worki, FAQ, Elasticsearch, manual, chatbot/mobile/CDN frontend after BE, docs |
| 김가영 | admin, department, admin dashboard |
| 김진혁 | ticket, TEAM_ADMIN transfer request, AI 연동 계약, chatbot sessions/messages/answer flow, docs |
| 이슬이 | auth, user, security, notification, point, esggrade, ESG metrics |
| 황희수 | frontend core flow |

챗봇 세션·메시지 저장과 AI RAG 연동·출처·전환 정책은 김진혁이 담당한다.

## 10. 지금 결정하면 좋은 것

| 결정 | 추천 |
|---|---|
| DB | 현재 의존성 기준 MariaDB로 MVP 시작. Vector Store는 별도 adapter로 둔다 |
| Vector Store | AI RAG는 Qdrant, BE 전문 검색은 Elasticsearch로 분리한다 |
| 인증 | 자체 JWT 먼저. SSO는 Phase 3 |
| RAG | 고객사별 LLM/Embedding provider + Qdrant. 출처 저장과 구조화된 `NO_RESULT` 전환을 필수로 둔다 |
| 배치 | 이벤트 기반 동기화 + 일일 정합성 점검으로 구성한다 |
| 모듈 분리 | 패키지 모듈러 모놀리스. 멀티모듈 Gradle은 MVP 이후 |
