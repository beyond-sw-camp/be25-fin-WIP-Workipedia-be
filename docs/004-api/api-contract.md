# API Contract

> 문서 유형: API Contract
> 상태: Draft
> 정본 위치: `docs/004-api/api-contract.md`
> 관련 문서: `docs/001-reference/prd.md`, `docs/001-reference/trd.md`, `docs/006-planning/wbs.md`
> 버전: v0.3
> 최종 수정: 2026-06-01

## 1. 목적

프론트엔드와 백엔드가 같은 요청/응답 형식을 기준으로 개발하기 위한 API 계약 초안이다.

이 문서는 확정 API 명세가 아니라, 2026-06-26 배포 목표까지 MVP 개발 충돌을 줄이기 위한 기준이다. API가 바뀌면 이 문서를 먼저 수정하고 담당자에게 공유한다.

## 2. 공통 규칙

### 2.1 Base URL

| 환경 | Base URL                       |
|---|--------------------------------|
| local | `http://localhost:8080/api/v1` |
| dev/staging | 미정                             |
| production | 미정                             |

### 2.2 인증

우리 서비스는 JWT(JSON Web Token) 기반 인증 방식을 사용한다.

```http
Authorization: Bearer <accessToken>
```

#### 로그인 인증 흐름

1. 사용자는 사번과 비밀번호를 입력하여 로그인한다.
2. 서버는 사용자 정보를 검증한 후 JWT 토큰을 발급한다.
3. 인증 성공 시 `Access Token`은 Response Body를 통해 반환된다.
4. 인증 성공 시 `Refresh Token`은 쿠키(Set-Cookie)를 통해 발급된다.
5. 서버는 발급한 `Refresh Token`을 Redis에 저장하여 관리한다.
6. 클라이언트는 로그인 응답 Body에서 `Access Token`을 받아 저장한다.
7. 이후 인증이 필요한 API를 호출할 때마다 `Authorization` 헤더에 `Access Token`을 포함하여 요청한다.
8. 서버는 전달받은 `Access Token`을 검증한 후 사용자 인증 및 권한 검사를 수행한다.

### 2.3 공통 응답

모든 API 응답 Body는 공통 응답 객체로 감싼다.
개별 API 명세의 `Response` 예시가 도메인 필드만 보여주는 경우에도 실제 응답에서는 아래 공통 응답의 `data` 안에 들어간다.

| 필드 | 타입 | 설명 |
|---|---|---|
| `code` | Int | HTTP 상태 코드. 예: `200`, `201`, `400`, `401`, `500` |
| `status` | String | 성공 시 HTTP 상태 이름, 실패 시 비즈니스 에러 코드. 예: `OK`, `CREATED`, `bad_request`, `ticket-001` |
| `message` | String | 응답 메시지 |
| `data` | Object / Array / null | 실제 응답 데이터. 단건은 객체, 목록은 배열 또는 페이지 객체, 응답 데이터가 없으면 `null` |

성공 응답:

```json
{
  "code": 200,
  "status": "OK",
  "message": "조회 성공",
  "data": {}
}
```

생성 성공 응답:

```json
{
  "code": 201,
  "status": "CREATED",
  "message": "생성 완료",
  "data": {
    "id": 1
  }
}
```

실패:

```json
{
  "code": 404,
  "status": "ticket-001",
  "message": "티켓을 찾을 수 없습니다.",
  "data": null
}
```

구현 기준:

- Spring Controller는 `ApiResponse<T>` 형태로 응답한다.
- `data`는 배열로 고정하지 않고 제네릭으로 둔다.
- 목록 조회에서 페이징이 필요하면 `data.content`, `data.pageInfo` 구조를 사용한다.
- 에러 응답의 `data`는 기본적으로 `null`로 둔다.
- HTTP status code와 Body의 `code` 값은 같은 값을 사용한다.
- 성공 응답의 `status`는 `HttpStatus.name()` 값을 사용한다. 예: `OK`, `CREATED`
- 실패 응답의 `status`는 `ErrorType.status`에 정의한 비즈니스 에러 코드를 사용한다.
- 공통 에러 코드는 `bad_request`, `unauthorized`, `forbidden`, `not_found`, `conflict`, `internal_error`를 사용한다.
- 도메인 에러 코드는 `{domain}-{number}` 형식을 사용한다. 예: `auth-001`, `ticket-001`, `worki-001`

### 2.4 페이지 응답

```json
{
  "code": 200,
  "status": "OK",
  "message": "성공",
  "data": {
    "content": [
      {}
    ],
    "pageInfo": {
      "page": 1,
      "size": 10,
      "totalElements": 0,
      "totalPages": 0,
      "hasNext": false,
      "hasPrevious": false
    }
  }
}
```

## 3. 담당자별 API 범위

| 영역 | 백엔드 담당 | 프론트 담당 |
|---|---|---|
| Auth | 이슬이 | 황희수 |
| 챗봇 세션/메시지 | 이슬이 | 황희수 |
| 챗봇 답변/RAG/전환 | 김진혁 | 황희수 |
| 워키 게시판 | 민정기 | 황희수 |
| FAQ | 민정기 | 황희수 |
| 알림 | 민정기 | 황희수 |
| 티켓 | 김진혁 | 황희수 |
| 티켓 지식화 | 김진혁, 김가영 | 황희수 |
| 관리자 대시보드 | 김가영 | 황희수 |
| 관리자 매뉴얼/부서/사용자 | 김가영 | 황희수 |
| 포인트 | 김가영 | 황희수 |
| ESG 등급 | 김가영 | 황희수 |
| ESG 지표 | 김가영 | 황희수 |

## 4. Auth API

담당: 이슬이

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/auth/signup` | 회원가입 | 불필요 |
| POST | `/auth/login` | 로그인 | 불필요 |
| POST | `/auth/token/refresh` | Access Token 재발급 | Refresh Cookie 필요 |
| POST | `/auth/logout` | 로그아웃 | Access Token 또는 Refresh Cookie 필요 |
| POST | `/auth/password-reset/code` | 비밀번호 재설정 인증코드 발송 | 불필요 |
| POST | `/auth/password-reset/code/verify` | 인증코드 확인 | 불필요 |
| PATCH | `/auth/password-reset` | 비밀번호 재설정 | 불필요 |
| GET | `/me` | 내 정보 | 필요 |

### POST `/auth/signup`

Request:

```json
{
  "employeeId": "20260001",
  "departmentId": 1,
  "email": "user@company.com",
  "password": "abc12345"
}
```

Response:

```json
{
  "code": 201,
  "status": "CREATED",
  "message": "회원가입 완료",
  "data": {
    "userId": 123,
    "role": "USER",
    "nickname": "눈물흘리는데이지",
    "status": "ACTIVE"
  }
}
```

### POST `/auth/login`

Request:

```json
{
  "employeeId": "20260001",
  "password": "abc12345"
}
```

Response:

```json
{
  "code": 200,
  "status": "OK",
  "message": "로그인 성공",
  "data": {
    "accessToken": "jwt-access-token",
    "userId": 123,
    "departmentId": 1,
    "role": "USER",
    "nickname": "눈물흘리는데이지",
    "status": "ACTIVE"
  }
}
```

Response Header:

```http
Set-Cookie: refreshToken=jwt-refresh-token; HttpOnly; Secure; SameSite=Lax; Path=/api/v1/auth
```

## 5. Chatbot API

담당: 이슬이, 김진혁

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/chatbot/sessions` | 챗봇 세션 생성 | 필요 |
| GET | `/chatbot/sessions` | 내 세션 목록 | 필요 |
| GET | `/chatbot/sessions/{sessionId}/messages` | 세션 메시지 조회 | 필요 |
| POST | `/chatbot/sessions/{sessionId}/messages` | 질문 전송 및 답변 생성 | 필요 |
| GET | `/chatbot/sessions/{sessionId}/messages/{messageId}/worki-support` | 워키 질문 등록 지원 (챗봇 메시지 기반 초안 반환) | 필요 |

### POST `/chatbot/sessions/{sessionId}/messages`

Request:

```json
{
  "content": "연차 신청은 어디서 하나요?"
}
```

Response:

```json
{
  "messageId": 101,
  "answer": "연차는 HR 시스템에서 신청할 수 있습니다.",
  "answerable": true,
  "references": [
    {
      "type": "MANUAL",
      "sourceId": 10,
      "title": "휴가 규정",
      "url": "/manuals/10",
      "chunkId": 1001
    }
  ],
  "nextAction": "SHOW_SOURCES"
}
```

근거 부족 응답:

```json
{
  "messageId": 102,
  "answer": "현재 등록된 문서에서 확실한 답변을 찾지 못했습니다.",
  "answerable": false,
  "references": [],
  "nextAction": "CREATE_WORKI",
  "draftQuestion": {
    "title": "연차 신청 관련 문의",
    "content": "연차 신청은 어디서 하나요?"
  }
}
```

요청 전환 응답:

```json
{
  "messageId": 103,
  "answer": "문서 검색만으로 해결하기 어렵습니다. 담당 부서 처리가 필요한 요청으로 전환할 수 있습니다.",
  "answerable": false,
  "references": [],
  "nextAction": "CREATE_TICKET",
  "draftTicket": {
    "title": "VPN 접속 오류 처리 요청",
    "content": "VPN 접속 오류 처리를 요청합니다."
  }
}
```

## 6. Worki API

담당: 민정기

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/worki/questions` | 질문 목록 | 필요 |
| POST | `/worki/questions` | 질문 등록 | 필요 |
| GET | `/worki/questions/{questionId}` | 질문 상세 | 필요 |
| PATCH | `/worki/questions/{questionId}` | 질문 수정 | 필요 |
| POST | `/worki/questions/{questionId}/answers` | 답변 등록 | 필요 |
| POST | `/worki/questions/{questionId}/ticket-answers` | 티켓 공식 답변 워키 등록 | 필요 |
| PATCH | `/worki/answers/{answerId}/adopt` | 답변 채택 | 필요 |
| PUT | `/worki/questions/{questionId}/reaction` | 좋아요/싫어요 | 필요 |

### POST `/worki/questions`

Request:

```json
{
  "title": "연차 신청 관련 문의",
  "content": "연차 신청은 어디서 하나요?",
  "sourceChatbotMessageId": 102
}
```

Response:

```json
{
  "questionId": 1,
  "title": "연차 신청 관련 문의",
  "status": "WAITING",
  "authorNickname": "노잇1234"
}
```

## 7. Ticket API

담당: 김진혁

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/tickets` | 티켓 생성 | 필요 |
| GET | `/tickets` | 티켓 목록 | 필요 |
| GET | `/tickets/{ticketId}` | 티켓 상세 | 필요 |
| PATCH | `/tickets/{ticketId}/status` | 티켓 상태 변경 | 필요 |
| PATCH | `/tickets/{ticketId}/assignee` | 팀원 담당자 배정 | TEAM_ADMIN |
| POST | `/tickets/{ticketId}/transfer-requests` | TEAM_ADMIN 티켓 이관 요청 | TEAM_ADMIN |
| PATCH | `/tickets/{ticketId}/refuse` | 티켓 반려 | TEAM_ADMIN |
| POST | `/tickets/{ticketId}/answers` | 담당 부서 공식 답변 | 필요 |
| POST | `/tickets/{ticketId}/knowledge-candidates` | 처리 완료 티켓 지식화 후보 등록 | 필요 |
| PATCH | `/knowledge-candidates/{candidateId}/review` | 지식화 후보 승인/반려 | TEAM_ADMIN |

### POST `/tickets`

Request:

```json
{
  "questionId": null,
  "sourceChatbotMessageId": 102,
  "type": "REQUEST",
  "categoryId": 3,
  "title": "VPN 접속 오류 처리 요청",
  "content": "VPN 접속 오류 처리를 요청합니다."
}
```

Response:

```json
{
  "ticketId": 1,
  "status": "ASSIGNED",
  "assignedDepartmentId": 5,
  "assignedDepartmentName": "IT지원팀",
  "routingConfidenceScore": 87.5,
  "routingDecision": "AUTO_ASSIGNED",
  "routingReasons": [
    "키워드: VPN, 접속 오류",
    "카테고리: 시스템 접근",
    "관련 문서: VPN 접속 장애 처리 가이드"
  ]
}
```

신뢰도 낮은 요청 Response:

```json
{
  "ticketId": 2,
  "status": "COMMON_QUEUE",
  "assignedDepartmentId": null,
  "assignedDepartmentName": null,
  "routingConfidenceScore": 63.0,
  "routingDecision": "COMMON_QUEUE",
  "candidateDepartments": [
    {
      "departmentId": 2,
      "departmentName": "자산관리팀",
      "confidenceScore": 63.0
    },
    {
      "departmentId": 6,
      "departmentName": "정보보안팀",
      "confidenceScore": 58.0
    }
  ]
}
```

### PATCH `/tickets/{ticketId}/assignee`

Request:

```json
{
  "assigneeId": 12,
  "memo": "VPN 계정 확인 후 처리 부탁드립니다."
}
```

Response:

```json
{
  "ticketId": 1,
  "status": "IN_PROGRESS",
  "assigneeId": 12,
  "assigneeNickname": "노잇4821"
}
```

### POST `/tickets/{ticketId}/transfer-requests`

Request:

```json
{
  "suggestedDepartmentId": 2,
  "reason": "법무 검토가 필요한 문의입니다."
}
```

Response:

```json
{
  "requestId": 1,
  "ticketId": 1,
  "transferStatus": "REQUESTED",
  "ticketStatus": "COMMON_QUEUE",
  "fromDepartmentId": 5,
  "fromDepartmentName": "경영지원팀",
  "suggestedDepartmentId": 2,
  "suggestedDepartmentName": "법무팀"
}
```

이관 요청 시 티켓은 다른 부서로 직접 이동하지 않고 공통 접수 큐로 이동한다. 이후 `SYSTEM_ADMIN`이 공통 접수 큐에서 담당 부서를 재배정한다.

### PATCH `/admin/common-queue/tickets/{ticketId}/department`

Request:

```json
{
  "departmentId": 2,
  "comment": "이관 사유 확인 후 법무팀으로 재배정합니다."
}
```

Response:

```json
{
  "ticketId": 1,
  "status": "ASSIGNED",
  "assignedDepartmentId": 2,
  "assignedDepartmentName": "법무팀"
}
```

### POST `/tickets/{ticketId}/knowledge-candidates`

Request:

```json
{
  "draftTitle": "VPN 접속 오류 처리 절차",
  "draftContent": "VPN 접속 오류가 발생하면 계정 상태와 보안 프로그램 실행 여부를 먼저 확인한 뒤 IT지원팀에 요청합니다."
}
```

Response:

```json
{
  "candidateId": 1,
  "ticketId": 1,
  "status": "REVIEW_REQUESTED"
}
```

### PATCH `/knowledge-candidates/{candidateId}/review`

Request:

```json
{
  "decision": "APPROVE",
  "reviewComment": "개인 정보 제거 확인. 워키 반영 승인합니다."
}
```

Response:

```json
{
  "candidateId": 1,
  "status": "PUBLISHED",
  "publishedWorkiQuestionId": 30
}
```

## 8. FAQ API

담당: 민정기

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/faq/worki/popular` | 인기 워키 | 필요 |
| GET | `/faq/manuals/popular` | 인기 매뉴얼 | 필요 |
| GET | `/faq/manuals/recent` | 최근 등록 매뉴얼 | 필요 |

## 9. Notification API

담당: 민정기

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/notifications` | 알림 목록 | 필요 |
| GET | `/notifications/unread-count` | 미읽은 알림 갯수 | 필요 |
| PATCH | `/notifications/{notificationId}/read` | 개별 읽음 | 필요 |
| PATCH | `/notifications/read-all` | 모두 읽음 | 필요 |
| DELETE | `/notifications/{notificationId}` | 알림 삭제 | 필요 |

> Phase 2: `GET /notifications/stream` (SSE 실시간 알림) — MVP는 DB 저장 + 조회 API 기반으로 시작 (ADR 007)

## 10. Point API

담당: 김가영

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/points/me` | 내 포인트 | 필요 |
| GET | `/points/histories` | 포인트 변동 이력 전체 | 필요 |
| GET | `/points/histories/earn` | 적립 내역 | 필요 |
| GET | `/points/histories/spend` | 소모 내역 | 필요 |
| GET | `/points/ranking` | 포인트 랭킹 | 필요 |

## 11. ESG Metrics API

담당: 김가영

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/esg/metrics/me` | 내 ESG/기여 지표 | 필요 |
| GET | `/admin/esg/metrics` | 관리자 ESG 운영 지표 | TEAM_ADMIN, SYSTEM_ADMIN |

Response:

```json
{
  "knowledgeShareCount": 12,
  "acceptedAnswerCount": 4,
  "estimatedSavedMinutes": 60,
  "esgScore": 320,
  "gradeName": "SILVER",
  "sourceBackedAnswerRate": 0.85,
  "ticketCompletionRate": 0.72
}
```

## 12. Admin API

담당: 김가영

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/admin/team/tickets` | 자기 팀 티켓 큐 | TEAM_ADMIN |
| GET | `/admin/team/knowledge-candidates` | 자기 팀 지식화 후보 목록 | TEAM_ADMIN |
| GET | `/admin/dashboard/summary` | 운영 대시보드 요약 | SYSTEM_ADMIN |
| GET | `/admin/dashboard/ticket-statistics` | 티켓 통계 | SYSTEM_ADMIN |
| GET | `/admin/common-queue/tickets` | 공통 접수 큐 | SYSTEM_ADMIN |
| PATCH | `/admin/common-queue/tickets/{ticketId}/department` | 공통 접수 큐 티켓 부서 배정 | SYSTEM_ADMIN |
| GET | `/admin/users` | 사용자 목록 | SYSTEM_ADMIN |
| PATCH | `/admin/users/{userId}/status` | 사용자 상태 변경 (활성/비활성) | SYSTEM_ADMIN |
| DELETE | `/admin/worki/questions/{questionId}` | 워키 질문 soft delete | TEAM_ADMIN, SYSTEM_ADMIN |
| GET | `/admin/manuals` | 매뉴얼 목록 | SYSTEM_ADMIN |
| POST | `/admin/manuals` | 매뉴얼 추가 | SYSTEM_ADMIN |
| PUT | `/admin/manuals/{manualId}` | 매뉴얼 수정 | SYSTEM_ADMIN |
| DELETE | `/admin/manuals/{manualId}` | 매뉴얼 삭제 | SYSTEM_ADMIN |
| GET | `/admin/departments` | 부서 목록 | SYSTEM_ADMIN |
| POST | `/admin/departments` | 부서 추가 | SYSTEM_ADMIN |
| DELETE | `/admin/departments/{departmentId}` | 부서 삭제 | SYSTEM_ADMIN |
| GET | `/admin/points` | 포인트 현황 조회 | SYSTEM_ADMIN |
| PATCH | `/admin/points/{employeeId}/deduct` | 포인트 차감 | SYSTEM_ADMIN |
| GET | `/admin/logs` | 관리자 작업 로그 | SYSTEM_ADMIN |
| GET | `/admin/esg/metrics` | ESG 지표 | SYSTEM_ADMIN |

## 13. 미정 항목

| 항목 | 상태 | 결정 필요자 |
|---|---|---|
| Refresh Token 저장소 | Redis 확정 | 이슬이 |
| SYSTEM_ADMIN 담당 조직 | 기본: 경영지원팀, 회사별 조정 가능 | 김가영, 팀 전체 |
| 티켓 자동 배정 점수 가중치 | 초안 확정 필요 | 김진혁 |
| 로컬 임베딩 모델 | 미정 | 김진혁, 팀 전체 |
| Elasticsearch 인덱스 차원수/similarity | 미정 (임베딩 모델 확정 후 결정) | 민정기, 김진혁 |
| 알림 구현 방식 | SSE 우선, 폴링 fallback | 민정기, 황희수 |
| 챗봇 세션 구조 | 세션 기반 확정, 이슬이와 최종 합의 필요 | 이슬이, 김진혁 |
