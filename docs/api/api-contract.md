# API Contract

> 문서 유형: API Contract
> 상태: Draft
> 정본 위치: `docs/api/api-contract.md`
> 관련 문서: `docs/reference/prd.md`, `docs/reference/trd.md`, `docs/planning/wbs.md`
> 버전: v0.1
> 최종 수정: 2026-05-28

## 1. 목적

프론트엔드와 백엔드가 같은 요청/응답 형식을 기준으로 개발하기 위한 API 계약 초안이다.

이 문서는 확정 API 명세가 아니라, 2026-06-26 배포 목표까지 MVP 개발 충돌을 줄이기 위한 기준이다. API가 바뀌면 이 문서를 먼저 수정하고 담당자에게 공유한다.

## 2. 공통 규칙

### 2.1 Base URL

| 환경 | Base URL |
|---|---|
| local | `http://localhost:8080/api` |
| dev/staging | 미정 |
| production | 미정 |

### 2.2 인증

```http
Authorization: Bearer <accessToken>
```

Access Token 저장 방식은 프론트/백엔드 협의 후 확정한다.

### 2.3 공통 응답

성공:

```json
{
  "data": {},
  "error": null,
  "meta": {
    "timestamp": "2026-05-28T10:00:00"
  }
}
```

실패:

```json
{
  "data": null,
  "error": {
    "code": "AUTH_INVALID_CREDENTIALS",
    "message": "로그인 정보가 올바르지 않습니다."
  },
  "meta": {
    "timestamp": "2026-05-28T10:00:00"
  }
}
```

### 2.4 페이지 응답

```json
{
  "data": {
    "items": [],
    "page": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0
  },
  "error": null,
  "meta": {
    "timestamp": "2026-05-28T10:00:00"
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
| 관리자 대시보드 | 김가영 | 황희수 |
| 포인트 | 김가영 | 황희수 |
| 뱃지 | 김가영 | 황희수 |
| ESG 지표 | 김가영 | 황희수 |

## 4. Auth API

담당: 이슬이

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/auth/signup` | 회원가입 | 불필요 |
| POST | `/auth/login` | 로그인 | 불필요 |
| POST | `/auth/logout` | 로그아웃 | 필요 |
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
  "userId": 1,
  "employeeId": "20260001",
  "nickname": "노잇1234",
  "role": "USER"
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
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token",
  "user": {
    "userId": 1,
    "nickname": "노잇1234",
    "role": "USER"
  }
}
```

## 5. Chatbot API

담당: 이슬이, 김진혁

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/chatbot/sessions` | 챗봇 세션 생성 | 필요 |
| GET | `/chatbot/sessions` | 내 세션 목록 | 필요 |
| GET | `/chatbot/sessions/{sessionId}/messages` | 세션 메시지 조회 | 필요 |
| POST | `/chatbot/sessions/{sessionId}/messages` | 질문 전송 및 답변 생성 | 필요 |

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

## 6. Worki API

담당: 민정기

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/worki/questions` | 질문 목록 | 필요 |
| POST | `/worki/questions` | 질문 등록 | 필요 |
| GET | `/worki/questions/{questionId}` | 질문 상세 | 필요 |
| PATCH | `/worki/questions/{questionId}` | 질문 수정 | 필요 |
| POST | `/worki/questions/{questionId}/answers` | 답변 등록 | 필요 |
| POST | `/worki/answers/{answerId}/accept` | 답변 채택 | 필요 |
| POST | `/worki/{targetType}/{targetId}/reactions` | 좋아요/싫어요 | 필요 |

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
| POST | `/tickets/{ticketId}/transfer-requests` | 티켓 이관 요청 | 필요 |
| PATCH | `/tickets/{ticketId}/transfer-requests/{requestId}` | 이관 승인/반려 | 필요 |
| POST | `/tickets/{ticketId}/answers` | 담당 부서 공식 답변 | 필요 |

### POST `/tickets`

Request:

```json
{
  "questionId": 1,
  "sourceChatbotMessageId": 102,
  "title": "연차 신청 담당 부서 문의",
  "content": "연차 신청 절차를 확인하고 싶습니다."
}
```

Response:

```json
{
  "ticketId": 1,
  "status": "PENDING",
  "assignedDepartmentId": 1,
  "assignedDepartmentName": "인사팀"
}
```

### POST `/tickets/{ticketId}/transfer-requests`

Request:

```json
{
  "targetDepartmentId": 2,
  "reason": "법무 검토가 필요한 문의입니다."
}
```

Response:

```json
{
  "requestId": 1,
  "ticketId": 1,
  "status": "TRANSFER_REQUESTED",
  "targetDepartmentId": 2,
  "targetDepartmentName": "법무팀"
}
```

### PATCH `/tickets/{ticketId}/transfer-requests/{requestId}`

Request:

```json
{
  "decision": "APPROVE",
  "comment": "법무팀으로 이관합니다."
}
```

Response:

```json
{
  "ticketId": 1,
  "status": "TRANSFERRED",
  "assignedDepartmentId": 2,
  "assignedDepartmentName": "법무팀"
}
```

## 8. FAQ API

담당: 민정기

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/faq/manuals/popular` | 인기 매뉴얼 | 필요 |
| GET | `/faq/worki/popular` | 인기 워키 | 필요 |
| GET | `/faq/summary` | 메인 FAQ 요약 | 필요 |

## 9. Notification API

담당: 민정기

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/notifications` | 알림 목록 | 필요 |
| GET | `/notifications/stream` | 실시간 알림 스트림(SSE 우선) | 필요 |
| PATCH | `/notifications/{notificationId}/read` | 개별 읽음 | 필요 |
| PATCH | `/notifications/read-all` | 모두 읽음 | 필요 |
| DELETE | `/notifications/{notificationId}` | 알림 삭제 | 필요 |

## 10. Point API

담당: 김가영

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/points/me` | 내 포인트 | 필요 |
| GET | `/points/me/history` | 내 포인트 이력 | 필요 |
| GET | `/points/ranking` | 포인트 랭킹 | 필요 |

## 11. Badge API

담당: 김가영

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/badges/me` | 내 뱃지 목록 | 필요 |
| GET | `/badges` | 전체 뱃지 기준 | 필요 |

기본 뱃지 조건:

| Badge | 조건 |
|---|---|
| `FIRST_QUESTION` | 첫 워키 질문 등록 |
| `FIRST_ACCEPTED_ANSWER` | 첫 채택 답변 |
| `ANSWER_HELPER` | 답변 5개 이상 |

## 12. ESG Metrics API

담당: 김가영

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/esg/metrics/me` | 내 ESG/기여 지표 | 필요 |
| GET | `/admin/esg/metrics` | 관리자 ESG 운영 지표 | ADMIN |

Response:

```json
{
  "knowledgeShareCount": 12,
  "acceptedAnswerCount": 4,
  "estimatedSavedMinutes": 60,
  "sourceBackedAnswerRate": 0.85,
  "ticketCompletionRate": 0.72
}
```

## 13. Admin API

담당: 김가영

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/admin/dashboard` | 관리자 대시보드 | ADMIN |
| GET | `/admin/users` | 사용자 목록 | ADMIN |
| PATCH | `/admin/users/{userId}/deactivate` | 사용자 비활성화 | ADMIN |
| DELETE | `/admin/worki/questions/{questionId}` | 워키 질문 soft delete | ADMIN |
| GET | `/admin/tickets` | 전체 티켓 조회 | ADMIN |
| GET | `/admin/logs` | 관리자 작업 로그 | ADMIN |
| GET | `/admin/points` | 포인트 현황 | ADMIN |
| GET | `/admin/badges` | 뱃지 현황 | ADMIN |
| GET | `/admin/esg/metrics` | ESG 지표 | ADMIN |

## 14. 미정 항목

| 항목 | 상태 | 결정 필요자 |
|---|---|---|
| Access/Refresh Token 저장 위치 | 미정 | 이슬이, 황희수 |
| refresh token API | 미정 | 이슬이 |
| 관리자 권한 범위 | 미정 | 김가영, 팀 전체 |
| 티켓 자동 배정 방식 | 미정 | 김진혁 |
| 로컬 임베딩 모델 | 미정 | 김진혁, 팀 전체 |
| Vector 저장 방식 | 미정 | 김진혁 |
| 알림 구현 방식 | SSE 우선, 폴링 fallback | 민정기, 황희수 |
