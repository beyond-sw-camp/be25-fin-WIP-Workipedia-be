# API Contract

> 문서 유형: API Contract
> 상태: Draft
> 정본 위치: `docs/004-api/api-contract.md`
> 관련 문서: `docs/001-reference/prd.md`, `docs/001-reference/trd.md`, `docs/006-planning/wbs.md`
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
| 티켓 지식화 | 김진혁, 김가영 | 황희수 |
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
    "role": "USER",
    "departmentId": 1
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
| PATCH | `/tickets/{ticketId}/assignee` | 팀원 담당자 배정 | TEAM_ADMIN |
| POST | `/tickets/{ticketId}/transfer-requests` | TEAM_ADMIN 티켓 이관 요청 | TEAM_ADMIN |
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
| GET | `/admin/esg/metrics` | 관리자 ESG 운영 지표 | TEAM_ADMIN, SYSTEM_ADMIN |

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
| GET | `/admin/team/tickets` | 자기 팀 티켓 큐 | TEAM_ADMIN |
| GET | `/admin/team/knowledge-candidates` | 자기 팀 지식화 후보 목록 | TEAM_ADMIN |
| GET | `/admin/dashboard` | 운영 대시보드 | SYSTEM_ADMIN |
| GET | `/admin/common-queue/tickets` | 공통 접수 큐 | SYSTEM_ADMIN |
| PATCH | `/admin/common-queue/tickets/{ticketId}/department` | 공통 접수 큐 티켓 부서 배정 | SYSTEM_ADMIN |
| GET | `/admin/users` | 사용자 목록 | SYSTEM_ADMIN |
| PATCH | `/admin/users/{userId}/deactivate` | 사용자 비활성화 | SYSTEM_ADMIN |
| DELETE | `/admin/worki/questions/{questionId}` | 워키 질문 soft delete | TEAM_ADMIN, SYSTEM_ADMIN |
| GET | `/admin/tickets/metrics` | 팀 단위 티켓 운영 지표 | SYSTEM_ADMIN |
| GET | `/admin/logs` | 관리자 작업 로그 | SYSTEM_ADMIN |
| GET | `/admin/points` | 포인트 현황 | SYSTEM_ADMIN |
| GET | `/admin/badges` | 뱃지 현황 | SYSTEM_ADMIN |
| GET | `/admin/esg/metrics` | ESG 지표 | SYSTEM_ADMIN |

## 14. 미정 항목

| 항목 | 상태 | 결정 필요자 |
|---|---|---|
| Access/Refresh Token 저장 위치 | 미정 | 이슬이, 황희수 |
| refresh token API | 미정 | 이슬이 |
| SYSTEM_ADMIN 담당 조직 | 기본: 경영지원팀, 회사별 조정 가능 | 김가영, 팀 전체 |
| 티켓 자동 배정 점수 가중치 | 초안 확정 | 김진혁 |
| 로컬 임베딩 모델 | 미정 | 김진혁, 팀 전체 |
| Vector 저장 방식 | 미정 | 김진혁 |
| 알림 구현 방식 | SSE 우선, 폴링 fallback | 민정기, 황희수 |
