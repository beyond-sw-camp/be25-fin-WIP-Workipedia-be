# API Contract

> 문서 유형: API Contract
> 상태: Draft
> 정본 위치: `docs/api/api-contract.md`
> 관련 문서: `docs/reference/prd.md`, `docs/reference/trd.md`, `docs/planning/wbs.md`, `docs/adr/013-object-storage-strategy.md`
> 버전: v0.4
> 최종 수정: 2026-06-09

## 1. 목적

프론트엔드와 백엔드가 같은 요청/응답 형식을 기준으로 개발하기 위한 API 계약 초안이다.

이 문서는 확정 API 명세가 아니라, 2026-06-26 배포 목표까지 MVP 개발 충돌을 줄이기 위한 기준이다. API가 바뀌면 이 문서를 먼저 수정하고 담당자에게 공유한다.

## 2. 공통 규칙

### 2.1 Base URL


| 환경          | Base URL                       |
| ----------- | ------------------------------ |
| local       | `http://localhost:8080/api/v1` |
| dev/staging | 미정                             |
| production  | 미정                             |


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

성공 응답은 `ResponseEntity<T>`로 직접 반환한다.
응답 Body를 `code`, `status`, `message`, `data` 형태의 공통 객체로 감싸지 않는다.

구현 기준:

- Spring Controller는 `ResponseEntity<T>`를 직접 반환한다.
- 생성 성공은 `ResponseEntity.status(HttpStatus.CREATED).body(response)`를 사용한다.
- 일반 조회/수정 성공은 `ResponseEntity.ok(response)`를 사용한다.
- 응답 데이터가 없는 성공 응답은 `ResponseEntity.ok().build()` 또는 `ResponseEntity.noContent().build()`를 사용한다.
- 목록 조회는 배열 또는 페이지 객체를 직접 반환한다.
- 에러 응답은 공통 예외 처리 구조를 따른다.
- 공통 에러 코드는 `bad_request`, `unauthorized`, `forbidden`, `not_found`, `conflict`, `internal_error`를 사용한다.
- 도메인 에러 코드는 `{domain}-{number}` 형식을 사용한다. 예: `auth-001`, `ticket-001`, `worki-001`

### 2.4 페이지 응답

## 3. 담당자별 API 범위


| 영역             | 백엔드 담당   | 프론트 담당 |
| -------------- | -------- | ------ |
| Auth           | 이슬이      | 황희수    |
| 챗봇 세션/메시지      | 김진혁      | 민정기    |
| 챗봇 답변/RAG/전환   | 김진혁      | 민정기    |
| 워키 게시판         | 민정기      | 황희수    |
| FAQ            | 민정기      | 황희수    |
| 알림             | 이슬이      | 황희수    |
| 티켓             | 김진혁      | 황희수    |
| 티켓 지식화         | 김진혁, 김가영 | 황희수    |
| 관리자 대시보드       | 김가영      | 황희수    |
| 관리자 매뉴얼/부서/사용자 | 김가영      | 황희수    |
| 포인트            | 김가영      | 황희수    |
| ESG 등급         | 김가영      | 황희수    |
| ESG 지표         | 김가영      | 황희수    |


## 4. Auth & Mypage API

담당: 이슬이

| Method | Path                               | 설명               | 인증               |
| ------ | ---------------------------------- | ---------------- | ---------------- |
| GET    | `/departments`                     | 회원가입 부서 목록 조회    | 불필요              |
| POST   | `/auth/signup/code`                | 회원가입 인증코드 발송     | 불필요              |
| POST   | `/auth/signup/code/verify`         | 회원가입 인증코드 확인     | 불필요              |
| POST   | `/auth/signup`                     | 회원가입             | 불필요              |
| POST   | `/auth/login`                      | 로그인              | 불필요              |
| POST   | `/auth/token/refresh`              | 토큰 재발급           | Refresh Token 필요 |
| POST   | `/auth/logout`                     | 로그아웃             | Access Token 필요  |
| POST   | `/auth/password-reset/code`        | 비밀번호 재설정 인증코드 발송 | 불필요              |
| POST   | `/auth/password-reset/code/verify` | 비밀번호 재설정 인증코드 확인 | 불필요              |
| PATCH  | `/auth/password-reset`             | 비밀번호 재설정         | 불필요              |
| GET    | `/me/profile`                      | 마이페이지 조회         | Access Token 필요  |
| PATCH  | `/me/notification-settings`        | 알림 설정 변경         | Access Token 필요  |
| GET    | `/me/tickets`                      | 내 발행 티켓 목록 조회    | Access Token 필요  |
| GET    | `/me/tickets/{ticketId}`           | 내 발행 티켓 상세 조회    | Access Token 필요  |


## 5. Chatbot API

담당: 김진혁


| Method | Path                                                               | 설명                            | 인증  |
| ------ | ------------------------------------------------------------------ | ----------------------------- | --- |
| POST   | `/chatbot/sessions`                                                | 챗봇 세션 생성                      | 필요  |
| GET    | `/chatbot/sessions`                                                | 내 세션 목록                       | 필요  |
| GET    | `/chatbot/sessions/{sessionId}/messages`                           | 세션 메시지 조회                     | 필요  |
| POST   | `/chatbot/sessions/{sessionId}/messages`                           | 질문 전송 및 답변 생성                 | 필요  |
| GET    | `/chatbot/sessions/{sessionId}/messages/{messageId}/worki-support` | 워키 질문 등록 지원 (챗봇 메시지 기반 초안 반환) | 필요  |


### AI 운영 API (계획)

담당: 김진혁


| Method | Path                             | 설명                                      | 인증           |
| ------ | -------------------------------- | ----------------------------------------- | -------------- |
| GET    | `/admin/ai-prompt-settings`      | 활성 `custom_prompt` 조회                 | SYSTEM_ADMIN   |
| PUT    | `/admin/ai-prompt-settings`      | `custom_prompt`과 활성 상태 변경           | SYSTEM_ADMIN   |
| GET    | `/admin/ai-tools`                | API/DB Query Tool 목록 조회               | SYSTEM_ADMIN   |
| POST   | `/admin/ai-tools`                | API Tool 등록 또는 개발자 승인 Tool 반영   | SYSTEM_ADMIN   |
| PATCH  | `/admin/ai-tools/{aiToolId}`     | Tool 설정·승인·활성 상태 변경              | SYSTEM_ADMIN   |
| POST   | `/admin/ai-tools/{aiToolId}/test`| Tool 테스트 실행                           | SYSTEM_ADMIN   |
| GET    | `/admin/manual-knowledge`        | 수기 지식과 동기화 상태 조회               | SYSTEM_ADMIN   |
| POST   | `/admin/manual-knowledge`        | 수기 지식 등록                             | SYSTEM_ADMIN   |
| POST   | `/admin/manual-knowledge/{id}/sync` | 실패한 ChromaDB 동기화 재시도           | SYSTEM_ADMIN   |

`base_prompt`, provider 설정, credential, DB 접속정보와 SQL 원문은 관리자 API로 변경하지 않는다. 위 API는 아직 Controller가 구현되지 않은 계획 계약이며, V16에는 `ai_tools` 테이블만 반영되어 있다.


## 6. Worki API

담당: 민정기


| Method | Path                                    | 설명     | 인증  |
| ------ | --------------------------------------- | ------ | --- |
| GET    | `/worki/questions`                      | 질문 목록  | 필요  |
| POST   | `/worki/questions`                      | 질문 등록  | 필요  |
| GET    | `/worki/questions/{questionId}`         | 질문 상세  | 필요  |
| PATCH  | `/worki/questions/{questionId}`         | 질문 수정  | 필요  |
| POST   | `/worki/questions/{questionId}/answers` | 답변 등록  | 필요  |
| POST   | `/worki/answers/{answerId}/accept`      | 답변 채택  | 필요  |
| POST   | `/worki/questions/{questionId}/like`    | 좋아요    | 필요  |
| DELETE | `/worki/questions/{questionId}/like`    | 좋아요 취소 | 필요  |


## 7. Ticket API

담당: 김진혁


| Method | Path                                            | 설명                           | 인증         |
| ------ | ----------------------------------------------- | ---------------------------- | ---------- |
| POST   | `/tickets`                                      | 티켓 생성                        | 필요         |
| GET    | `/tickets`                                      | 티켓 목록, 상태/부서 필터 조회           | 필요         |
| GET    | `/tickets/{ticketId}`                           | 티켓 상세                        | 필요         |
| PATCH  | `/tickets/{ticketId}/status`                    | 티켓 상태 변경                     | 필요         |
| PATCH  | `/tickets/{ticketId}/assignee`                  | 팀원 담당자 배정                    | TEAM_ADMIN |
| POST   | `/tickets/{ticketId}/transfer-requests`         | TEAM_ADMIN 티켓 이관 요청          | TEAM_ADMIN |
| PATCH  | `/tickets/{ticketId}/refuse`                    | 티켓 반려                        | TEAM_ADMIN |
| POST   | `/tickets/{ticketId}/answers`                   | 담당 부서 공식 답변                  | 필요         |
| POST   | `/admin/team/tickets/{ticketId}/knowledge-data` | 처리 완료 티켓 지식화 승인 및 지식화 데이터 등록 | TEAM_ADMIN |
| PATCH  | `/admin/team/knowledge-data/{knowledgeDataId}`  | 지식화 데이터 질문/답변 수정             | TEAM_ADMIN |


신뢰도 낮은 요청 Response:

```json
{
  "ticketId": 2,
  "status": "COMMON_QUEUE",
  "priority": "MEDIUM",
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

### GET `/tickets`

티켓 목록을 조회한다. 프론트엔드는 같은 엔드포인트에서 상태별, 부서별 필터를 조합해 사용한다.

Query Parameters:


| 이름             | 타입     | 필수  | 설명                                                  |
| -------------- | ------ | --- | --------------------------------------------------- |
| `status`       | string | 아니오 | 티켓 상태. 예: `COMMON_QUEUE`, `ASSIGNED`, `IN_PROGRESS` |
| `departmentId` | number | 아니오 | 담당 부서 ID. `assignedDepartmentId` 기준으로 조회한다.         |
| `page`         | number | 아니오 | 페이지 번호. 기본값은 `1`이다.                                 |
| `size`         | number | 아니오 | 페이지 크기. 기본값은 `10`이다.                                |


Request 예시:

```http
GET /api/v1/tickets?status=COMMON_QUEUE&departmentId=1&page=1&size=10
```

Response:

```json
{
  "content": [
    {
      "ticketId": 5,
      "status": "COMMON_QUEUE",
      "priority": "MEDIUM",
      "assignedDepartmentId": null,
      "assignedDepartmentName": null,
      "routingConfidenceScore": null,
      "routingDecision": "COMMON_QUEUE",
      "routingReasons": [],
      "candidateDepartments": [],
      "sourceChatbotMessageId": null,
      "title": "테스트 티켓 제목",
      "content": "테스트 티켓 내용",
      "assigneeId": null,
      "createdAt": "2026-06-04T17:01:49",
      "updatedAt": "2026-06-04T17:01:49"
    }
  ],
  "pageInfo": {
    "page": 1,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

비고:

- `departmentId`는 조회 필터이며, 부서 배정/재배정 동작을 의미하지 않는다.
- 공통 접수 큐의 부서 재배정은 `PATCH /admin/common-queue/tickets/{ticketId}/department`를 사용한다.

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
  "priority": "MEDIUM",
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

### POST `/admin/team/tickets/{ticketId}/knowledge-data`

Request:

```json
{
  "title": "VPN 접속 오류 처리 절차",
  "content": "VPN 접속 오류 발생 시 계정 상태와 보안 프로그램 실행 여부를 먼저 확인한 뒤 IT지원팀에 요청합니다."
}
```

Response:

```json
{
  "knowledgeDataId": 1,
  "ticketId": 1,
  "title": "VPN 접속 오류 처리 절차",
  "departmentId": 3,
  "approvedBy": 1
}
```

### PATCH `/admin/team/knowledge-data/{knowledgeDataId}`

Request:

```json
{
  "title": "VPN 접속 오류 처리 절차",
  "content": "VPN 접속 오류 조치 절차를 수정합니다."
}
```

Response:

```json
{
  "knowledgeDataId": 1,
  "title": "VPN 접속 오류 처리 절차",
  "updatedAt": "2026-06-09T10:30:00"
}
```

### Attachment API

담당: 김진혁

파일 바이너리는 RDB가 아니라 Object Storage에 저장한다. FE는 provider 종류와 무관하게 presigned URL 흐름을 사용하며, 실제 provider는 배포 설정의 `storage.provider`로 선택한다.

#### 현재 구현된 Storage API

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/api/v1/storage/presigned-upload` | 업로드 URL, `objectKey`, `publicUrl` 발급 | 필요 |
| GET | `/api/v1/storage/presigned-download?objectKey=...` | 다운로드 URL 발급 | 필요 |
| DELETE | `/api/v1/storage?objectKey=...` | object 삭제 | 필요 |

Presigned upload request:

```json
{
  "fileName": "error-screen.png",
  "contentType": "image/png"
}
```

Presigned upload response:

```json
{
  "uploadUrl": "https://object-storage.example.com/...",
  "objectKey": "tickets/replies/uuid/error-screen.png",
  "publicUrl": "https://files.example.com/tickets/replies/uuid/error-screen.png"
}
```

FE는 `uploadUrl`에 파일을 직접 PUT한 뒤 `objectKey`를 첨부 메타데이터 등록 API에 전달한다.

#### 첨부 메타데이터 API(예정)

| Method | Path                          | 설명                                      | 인증  |
| ------ | ----------------------------- | ----------------------------------------- | --- |
| POST   | `/attachments`                | 업로드 완료 object 메타데이터 등록, `attachmentId` 반환 | 필요  |
| GET    | `/attachments/{attachmentId}` | 첨부 메타데이터와 조회 URL 반환                    | 필요  |



| Field         | Type   | 설명                                         |
| ------------- | ------ | ------------------------------------------ |
| `objectKey`   | string | presigned upload API에서 발급받은 object key       |
| `fileName`    | string | 원본 파일명                                     |
| `contentType` | string | 허용된 이미지 MIME                               |
| `fileSize`    | number | 업로드 파일 크기(byte)                            |
| `targetType`  | string | `TICKET` 등 첨부 대상                           |
| `targetId`    | number | 이미 생성된 대상에 연결할 때 사용. 티켓 생성 전 업로드 시 null 가능 |


Response:

```json
{
  "attachmentId": 1,
  "objectKey": "tickets/replies/uuid/error-screen.png",
  "fileName": "error-screen.png",
  "contentType": "image/png",
  "fileSize": 123456,
  "downloadUrl": "/attachments/1"
}
```

## 8. FAQ API

담당: 민정기


| Method | Path                   | 설명        | 인증  |
| ------ | ---------------------- | --------- | --- |
| GET    | `/faq/worki/popular`   | 인기 워키     | 필요  |
| GET    | `/faq/manuals/popular` | 인기 매뉴얼    | 필요  |
| GET    | `/faq/manuals/recent`  | 최근 등록 매뉴얼 | 필요  |


## 9. Notification API

담당: 민정기


| Method | Path                                   | 설명        | 인증  |
| ------ | -------------------------------------- | --------- | --- |
| GET    | `/notifications`                       | 알림 목록     | 필요  |
| GET    | `/notifications/unread-count`          | 미읽은 알림 갯수 | 필요  |
| PATCH  | `/notifications/{notificationId}/read` | 개별 읽음     | 필요  |
| PATCH  | `/notifications/read-all`              | 모두 읽음     | 필요  |
| DELETE | `/notifications/{notificationId}`      | 알림 삭제     | 필요  |


> Phase 2: `GET /notifications/stream` (SSE 실시간 알림) — MVP는 DB 저장 + 조회 API 기반으로 시작 (ADR 007)

## 9-1. Flash Chat API

담당: 김진혁, 민정기, 김가영

> MVP는 메시지/답장과 관리자 운영 정책을 구현한다. 좋아요 반응(`/app/flash-chat/react`)은 MVP 이후 범위다.


| Type            | Path                    | 설명                        | 인증  |
| --------------- | ----------------------- | ------------------------- | --- |
| WS Connect      | `/ws/flash-chat`        | STOMP 연결 (SockJS 지원)      | 필요  |
| WS Connect      | `/ws/flash-chat-native` | Native WebSocket STOMP 연결 | 필요  |
| REST GET        | `/flash-chat/messages`  | 현재 활성 메시지 목록              | 필요  |
| STOMP Subscribe | `/topic/flash-chat`     | 메시지/삭제 이벤트 수신             | 필요  |
| STOMP Send      | `/app/flash-chat/send`  | 메시지/답장 전송                 | 필요  |


### GET `/flash-chat/messages`

Response:

```json
{
  "messages": [
    {
      "id": "018f6c9d-7b4f-7a9a-9c15-1b0f4b5ad111",
      "userId": 123,
      "nickname": "노잇4821",
      "content": "연차 반차 차이가 뭐예요?",
      "replyToId": null,
      "createdAt": "2026-06-08T10:00:00",
      "expiresAt": "2026-06-08T10:10:00"
    }
  ]
}
```

### `/app/flash-chat/send`

Payload:

```json
{
  "content": "연차 반차 차이가 뭐예요?",
  "replyToId": null
}
```

### `/topic/flash-chat` 브로드캐스트

메시지 전송 이벤트:

```json
{
  "type": "MESSAGE",
  "id": "018f6c9d-7b4f-7a9a-9c15-1b0f4b5ad111",
  "userId": 1,
  "nickname": "노잇0001",
  "content": "연차 반차 차이가 뭐예요?",
  "replyToId": null,
  "createdAt": "2026-06-08T10:00:00",
  "expiresAt": "2026-06-08T10:10:00"
}
```

관리자 강제 삭제 이벤트:

```json
{
  "type": "DELETE",
  "id": "018f6c9d-7b4f-7a9a-9c15-1b0f4b5ad111"
}
```

## 10. Point API

담당: 이슬이


| Method | Path                  | 설명           | 인증              |
| ------ |-----------------------|--------------|-----------------|
| GET    | `/me/points`          | 현재 보유 포인트 조회 | Access Token 필요 |
| GET    | `/me/point-histories` | 포인트 변동 내역 조회 | Access Token 필요 |


## 11. ESG Metrics API

담당: 김가영


| Method | Path                 | 설명            | 인증                       |
| ------ | -------------------- | ------------- | ------------------------ |
| GET    | `/esg/metrics/me`    | 내 ESG/기여 지표   | 필요                       |
| GET    | `/admin/esg/metrics` | 관리자 ESG 운영 지표 | TEAM_ADMIN, SYSTEM_ADMIN |


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

팀 관리자 대시보드 


| Method | Path                                           | 설명                        | 인증         |
| ------ | ---------------------------------------------- | ------------------------- | ---------- |
| GET    | `/admin/team/dashboard/knowledge-trend`        | 월별 지식화 승인 건수 추이 조회        | TEAM_ADMIN |
| GET    | `/admin/team/dashboard/chatbot-ticket-trend`   | 월별 AI 챗봇 배정 티켓 건수 추이 조회   | TEAM_ADMIN |
| GET    | `/admin/team/tickets?status=COMPLETED`         | 지식화 승인 가능한 처리 완료 티켓 목록 조회 | TEAM_ADMIN |
| GET    | `/admin/team/knowledge-data`                   | 승인된 지식화 데이터 목록 조회         | TEAM_ADMIN |
| PATCH  | `/admin/team/knowledge-data/{knowledgeDataId}` | 지식화 데이터 질문/답변 수정          | TEAM_ADMIN |
| DELETE | `/admin/team/knowledge-data/{knowledgeDataId}` | 지식화 데이터 삭제                | TEAM_ADMIN |
| GET    | `/admin/team/tickets/summary`                  | 우리 부서 티켓 요약 정보 조회         | TEAM_ADMIN |
| GET    | `/admin/team/tickets`                          | 우리 부서 배정 티켓 목록 조회         | TEAM_ADMIN |
| GET    | `/admin/team/tickets/{ticketId}`               | 우리 부서 티켓 상세 조회            | TEAM_ADMIN |
| POST   | `/admin/team/tickets/{ticketId}/transfer`      | 티켓 이관 사유 입력 후 공통 접수 큐로 이동 | TEAM_ADMIN |


전체 관리자 대시보드


| Method | Path                                                | 설명                 | 인증           |
| ------ | --------------------------------------------------- | ------------------ | ------------ |
| GET    | `/admin/dashboard/auto-routing-rate`                | 월별 챗봇 자동 배정률 추이 조회 | SYSTEM_ADMIN |
| GET    | `/admin/dashboard/ticket-trend`                     | 월별 전체 티켓 발행 추이 조회  | SYSTEM_ADMIN |
| GET    | `/admin/dashboard/department-statistics`            | 부서별 티켓 현황 조회       | SYSTEM_ADMIN |
| GET    | `/admin/dashboard/routing-statistics`               | 부서별 자동 배정 성공률 조회   | SYSTEM_ADMIN |
| GET    | `/admin/common-queue/tickets`                       | 공통 접수 큐 목록 조회      | SYSTEM_ADMIN |
| PATCH  | `/admin/common-queue/tickets/{ticketId}/department` | 공통 접수 큐 티켓 부서 배정   | SYSTEM_ADMIN |


관리자 설정


| Method | Path                                            | 설명                            | 인증           |
| ------ | ----------------------------------------------- | ----------------------------- | ------------ |
| GET    | `/admin/settings/summary`                       | 전체 사용자 수, 당일 로그인 수, 총 문서 수 조회 | SYSTEM_ADMIN |
| GET    | `/admin/points/search`                          | 사번으로 사용자 포인트 조회               | SYSTEM_ADMIN |
| PATCH  | `/admin/points/{employeeId}/deduct`             | 포인트 차감                        | SYSTEM_ADMIN |
| DELETE | `/admin/worki/questions/{questionId}`           | 워키 게시글 관리자 삭제 및 작성자 포인트 차감    | SYSTEM_ADMIN |
| GET    | `/admin/departments`                            | 관리자 부서 목록 조회                  | SYSTEM_ADMIN |
| POST   | `/admin/departments`                            | 부서 등록                         | SYSTEM_ADMIN |
| PATCH  | `/admin/departments/{departmentId}`             | 부서 정보 수정                      | SYSTEM_ADMIN |
| DELETE | `/admin/departments/{departmentId}`             | 부서 삭제                         | SYSTEM_ADMIN |
| PATCH  | `/admin/departments/routing-prompt/instruction` | 부서 라우팅 프롬프트                   | SYSTEM_ADMIN |
| GET    | `/admin/users/search`                           | 사번으로 사용자 조회                   | SYSTEM_ADMIN |
| PATCH  | `/admin/users/{userId}/status`                  | 사용자 활성화/비활성화 변경               | SYSTEM_ADMIN |
| GET    | `/admin/manuals`                                | 매뉴얼 목록 조회                     | SYSTEM_ADMIN |
| POST   | `/admin/manuals`                                | 매뉴얼 등록 (본문 직접 입력)             | SYSTEM_ADMIN |
| POST   | `/admin/manuals/pdf`                            | 매뉴얼 등록 (PDF 업로드)              | SYSTEM_ADMIN |
| GET    | `/admin/manuals/{manualId}`                     | 매뉴얼 상세 조회                     | SYSTEM_ADMIN |
| PATCH  | `/admin/manuals/{manualId}`                     | 매뉴얼 수정 및 신규 버전 등록             | SYSTEM_ADMIN |
| PATCH  | `/admin/manuals/{manualId}/pdf`                 | 매뉴얼 본문 PDF 교체                 | SYSTEM_ADMIN |
| DELETE | `/admin/manuals/{manualId}`                     | 매뉴얼 삭제                        | SYSTEM_ADMIN |
| GET    | `/admin/flash-chat/policy`                      | TTL, 쿨다운, 금지어 정책 조회           | SYSTEM_ADMIN |
| PATCH  | `/admin/flash-chat/policy`                      | TTL, 쿨다운, 금지어 정책 일괄 변경        | SYSTEM_ADMIN |
| DELETE | `/admin/flash-chat/messages/{messageId}`        | Flash Chat 메시지 강제 삭제          | SYSTEM_ADMIN |


## 13.매뉴얼 (Manual)

매뉴얼은 **본문 직접 입력**과 **PDF 업로드** 두 가지 방식으로 등록/수정할 수 있다.
PDF 업로드 시 서버가 텍스트를 추출해 본문(`content`)으로 저장하고, **원본 PDF는 설정된 Object Storage(R2/S3/MinIO)에 보관**한 뒤 접근 URL(`fileUrl`)을 응답에 담는다.
모든 매뉴얼 API는 `SYSTEM_ADMIN` 권한이 필요하다. (권한이 없으면 `403 manual-002`)


| Method | Path                            | 설명                | 인증           |
| ------ | ------------------------------- | ----------------- | ------------ |
| GET    | `/admin/manuals`                | 매뉴얼 목록 조회         | SYSTEM_ADMIN |
| POST   | `/admin/manuals`                | 매뉴얼 등록 (본문 직접 입력) | SYSTEM_ADMIN |
| POST   | `/admin/manuals/pdf`            | 매뉴얼 등록 (PDF 업로드)  | SYSTEM_ADMIN |
| GET    | `/admin/manuals/{manualId}`     | 매뉴얼 상세 조회         | SYSTEM_ADMIN |
| PATCH  | `/admin/manuals/{manualId}`     | 매뉴얼 수정 (부분 수정)    | SYSTEM_ADMIN |
| PATCH  | `/admin/manuals/{manualId}/pdf` | 매뉴얼 본문 PDF 교체     | SYSTEM_ADMIN |
| DELETE | `/admin/manuals/{manualId}`     | 매뉴얼 삭제            | SYSTEM_ADMIN |


공통 응답 객체 `ManualDetailResponse`:

```json
{
  "manualId": 12,
  "departmentId": 1,
  "title": "사내 메신저 사용 가이드",
  "content": "## 1. 로그인\n...",
  "status": "PUBLISHED",
  "sourceUrl": "https://intra.example.com/manuals/123",
  "fileUrl": "https://files.example.com/manuals/3f1c.../guide.pdf",
  "version": "v1.0",
  "createdBy": 1,
  "createdAt": "2026-06-09T10:00:00",
  "updatedAt": "2026-06-09T10:00:00"
}
```

- 본문을 직접 입력해 등록한 매뉴얼은 `fileUrl`이 `null`이다.

### GET `/admin/manuals`

매뉴얼 목록을 페이지로 조회한다. (최신 등록순)

- Query: `page`(기본 1), `size`(기본 10, 최대 100), `status`(선택: `DRAFT`/`PUBLISHED`/`ARCHIVED`/`DELETED`)
- `status`를 생략하면 삭제되지 않은 전체 상태를 반환한다.
- Response: `200 OK`, 페이지 응답(2.4) 안의 `content`는 `ManualSummaryResponse` 배열 (`content` 본문 필드 제외).

### POST `/admin/manuals`

본문(`content`)을 직접 입력해 매뉴얼을 등록한다.

Request:

```json
{
  "departmentId": 1,
  "title": "사내 메신저 사용 가이드",
  "content": "## 1. 로그인\n...",
  "status": "PUBLISHED",
  "sourceUrl": "https://intra.example.com/manuals/123",
  "version": "v1.0"
}
```

- `title`(최대 255), `content`는 필수이다.
- `status`를 생략하면 `PUBLISHED`로 등록된다.
- `departmentId`, `sourceUrl`(최대 500), `version`(최대 50)은 선택값이다.
- Response: `201 Created`, `ManualDetailResponse`.

### POST `/admin/manuals/pdf`

PDF를 업로드해 매뉴얼을 등록한다. `Content-Type: multipart/form-data`.


| 필드             | 타입        | 필수  | 설명                 |
| -------------- | --------- | --- | ------------------ |
| `file`         | file(PDF) | 필수  | 업로드할 PDF (최대 20MB) |
| `title`        | string    | 필수  | 매뉴얼 제목 (최대 255)    |
| `departmentId` | number    | 선택  | 부서 ID              |
| `status`       | string    | 선택  | 기본 `PUBLISHED`     |
| `sourceUrl`    | string    | 선택  | 원본 출처 링크 (최대 500)  |
| `version`      | string    | 선택  | 버전 (최대 50)         |


- PDF가 아니거나 추출된 텍스트가 비어 있으면 `400 manual-003`.
- 추출한 텍스트가 본문(`content`)에 저장되고, 원본 PDF는 선택된 Object Storage에 저장되어 `fileUrl`로 반환된다.
- Response: `201 Created`, `ManualDetailResponse`.

### GET `/admin/manuals/{manualId}`

매뉴얼 상세를 조회한다. (관리자는 모든 상태 조회 가능)

- Response: `200 OK`, `ManualDetailResponse`.
- 존재하지 않거나 삭제된 매뉴얼이면 `404 manual-001`.

### PATCH `/admin/manuals/{manualId}`

매뉴얼을 부분 수정한다. 요청에서 `null`인 필드는 변경하지 않는다.

Request:

```json
{
  "title": "사내 메신저 사용 가이드 (개정)",
  "status": "ARCHIVED",
  "version": "v1.1"
}
```

- Response: `200 OK`, `ManualDetailResponse`.

### PATCH `/admin/manuals/{manualId}/pdf`

새 PDF를 업로드해 기존 매뉴얼의 본문(`content`)을 교체한다. `Content-Type: multipart/form-data`.

- form field는 `POST /admin/manuals/pdf`와 동일하되 `title` 포함 모든 필드가 선택값이다. (`file`만 필수)
- 본문 교체와 함께 **Object Storage의 기존 PDF는 새 파일로 교체(이전 파일 삭제)** 된다.
- Response: `200 OK`, `ManualDetailResponse`.

### DELETE `/admin/manuals/{manualId}`

매뉴얼을 소프트 삭제한다. (`status`를 `DELETED`로 변경)

- Object Storage에 보관된 원본 PDF가 있으면 함께 삭제한다.
- Response: `204 No Content`.

### GET `/admin/flash-chat/policy`

Response:

```json
{
  "messageTtlSeconds": 600,
  "sendCooldownSeconds": 0,
  "bannedWords": []
}
```

### PATCH `/admin/flash-chat/policy`

Request:

```json
{
  "messageTtlSeconds": 600,
  "sendCooldownSeconds": 0,
  "bannedWords": ["금지어"]
}
```

- `messageTtlSeconds`는 60초 이상이어야 한다.
- `sendCooldownSeconds`가 0이면 쿨다운을 적용하지 않는다.
- 정책 변경은 `FLASH_CHAT_CONFIG_UPDATE` action type으로 `admin_logs`에 기록한다.

### DELETE `/admin/flash-chat/messages/{messageId}`

- Redis Hash와 Sorted Set에서 메시지를 제거한다.
- 삭제 결과를 `/topic/flash-chat`에 `DELETE` 이벤트로 브로드캐스트한다.
- 강제 삭제는 `FLASH_CHAT_MESSAGE_DELETE` action type으로 `admin_logs`에 기록한다.

## 14. 미정 항목


| 항목                               | 상태                      | 결정 필요자    |
| -------------------------------- | ----------------------- | --------- |
| Refresh Token 저장소                | Redis 확정                | 이슬이       |
| SYSTEM_ADMIN 담당 조직               | 기본: 경영지원팀, 회사별 조정 가능    | 김가영, 팀 전체 |
| 티켓 자동 배정 점수 가중치                  | 초안 확정 필요                | 김진혁       |
| 로컬 임베딩 모델                        | 미정                      | 김진혁, 팀 전체 |
| Elasticsearch 인덱스 차원수/similarity | 미정 (임베딩 모델 확정 후 결정)     | 민정기, 김진혁  |
| 알림 구현 방식                         | SSE 우선, 폴링 fallback     | 이슬이, 황희수  |
| 챗봇 세션 구조                         | 세션 기반 확정, 이슬이와 최종 합의 필요 | 이슬이, 김진혁  |
| Flash Chat 최대 활성 메시지 수           | 미정                      | 김진혁, 김가영  |
| Object Storage orphan object 정리 정책 | 메타데이터 저장 실패·교체 실패 보상 방식 확정 필요 | 김진혁, 팀 전체 |
