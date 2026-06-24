# API Contract

> 문서 유형: API Contract
> 상태: Draft
> 정본 위치: `docs/api/api-contract.md`
> 관련 문서: `docs/reference/prd.md`, `docs/reference/trd.md`, `docs/planning/wbs.md`, `docs/adr/013-object-storage-strategy.md`
> 버전: v0.6
> 최종 수정: 2026-06-15

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
| 관리자 매뉴얼        | 민정기      | 황희수    |
| 관리자 부서         | 김가영      | 황희수    |
| 관리자 사용자        | 이슬이      | 황희수    |
| 포인트            | 이슬이      | 황희수    |
| ESG 등급         | 이슬이      | 황희수    |
| ESG 지표         | 이슬이      | 황희수    |


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
| POST   | `/chatbot/sessions/{sessionId}/messages/stream`                    | 질문 전송 및 답변 스트리밍(SSE, 타자 효과)   | 필요  |
| GET    | `/chatbot/sessions/{sessionId}/messages/{messageId}/worki-support` | 워키 질문 등록 지원 (챗봇 메시지 기반 초안 반환) | 필요  |

챗봇 세션 API 공통 규칙:

- 세션과 메시지는 인증 사용자 본인 소유 데이터만 조회·생성할 수 있다.
- 질문은 공백을 허용하지 않으며 최대 2,000자다.
- 메시지 응답은 `senderType`, `content`, `answerable`, `nextAction`, `referencesJson`, `createdAt`을 포함한다.
- 삭제된 세션은 조회하지 않으며 없는 세션은 404, 타인 세션은 403을 반환한다.
- 질문 전송 시 BE가 이전 대화와 활성 `custom_prompt`를 AI 서버에 전달하고 AI 답변을 저장한다.
- 스트리밍 엔드포인트(`.../messages/stream`)는 `text/event-stream`으로 응답한다. `token` 이벤트(`{"content":"..."}`)를 토큰 단위로 반복 전송한 뒤, 마지막에 `done` 이벤트로 위 메시지 응답 객체(저장된 `messageId` 포함) 1건을 전송한다. 프론트는 `token`으로 글자를 출력하고 `done`을 최종 확정값으로 사용한다. AI 호출 실패 시에도 안내 메시지를 저장하고 `done`으로 전달한다.
- 일반/스트리밍 엔드포인트 모두 답변·USER 메시지 저장 로직은 동일하며, 빈 답변이면 워키 질문 등록 유도 메시지로 대체한다.


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

`base_prompt`, provider 설정, credential, DB 접속정보와 SQL 원문은 관리자 API로 변경하지 않는다. 위 API는 아직 Controller가 구현되지 않은 계획 계약이며, V16에는 `ai_tools` 테이블만 반영되어 있다.

### BE → AI 내부 API

> 아래 경로는 프론트엔드 공개 API가 아니다. BE가 `AI_BASE_URL`의 AI 서버를 내부 호출하며 JSON 필드는 camelCase를 사용한다.

| Method | AI Path | 용도 | 핵심 계약 |
|---|---|---|---|
| POST | `/api/v1/chat` | 챗봇 추론 | `question`, 선택적 `customPrompt`, 최근 `sessionContext` → `answer`, `sources`, `route`, `action`, `stepHistory` |
| POST | `/api/v1/chat/stream` | 챗봇 추론(스트리밍) | 요청 본문은 `/api/v1/chat`과 동일. 응답은 `text/event-stream`. 아래 SSE 계약 참조 |
| POST | `/api/v1/tickets/routing` | 티켓 부서 추천 | `title`, `content`, 선택적 `sourceChatbotMessageId` → 배정 부서, 후보 Top 3, 점수·margin, decision |
| POST | `/api/v1/department/routing-prompt` | 부서 R&R 문장 생성 | `instruction`, 전체 부서 `targets` → 변경 대상 `results` |
| POST | `/api/v1/knowledge/sync` | 부서 R&R·승인 사례 동기화 | `sourceId`, `sourceType`, 제목·내용·부서 메타데이터 → `syncedChunks` |
| DELETE | `/api/v1/knowledge/{sourceId}?sourceType=...` | 라우팅 지식 삭제 | 없는 데이터도 성공하며 `deletedChunks: 0` 반환 |
| POST | `/api/v1/documents/ingest` | 파일 매뉴얼 인덱싱 | multipart `source_id`, `source_type`, `title`, `files` → `source_id`, `indexed_chunks` |
| POST | `/api/v1/documents/ingest-text` | 직접입력 매뉴얼·워키·승인 지식·수기 지식 텍스트 인덱싱 | JSON 본문 `sourceId`, `sourceType`, `title`, `content` → `source_id`, `indexed_chunks` |
| DELETE | `/api/v1/documents/{sourceId}?source_type=...` | 문서 인덱스 삭제 | 문서의 Qdrant 청크 삭제 |

`/api/v1/chat/stream` SSE 계약 (AI 서버 구현 완료):

- 응답 `Content-Type`은 `text/event-stream`. event 이름 없이 `data:` 프레임만 보내며, JSON 안의 `type` 필드로 종류를 구분한다.

  ```
  data: {"type":"token","content":"안녕"}

  data: {"type":"token","content":"하세요"}

  data: {"type":"done","route":"...","action":"CREATE_TICKET","sources":[...],"step_history":[...]}
  ```

- `token`: `{"type":"token","content":"<조각>"}`. BE는 조각을 이어붙여 최종 답변을 만든다.
- `done`: 토큰이 모두 끝난 뒤 1회. `sources`(snake_case 키), `route`, `action`, `step_history`를 담는다. BE는 이때 누적 답변 + 메타로 DB에 저장한다.
- `error`: `{"type":"error","message":"<사용자용 안전 메시지>"}`. BLOCKED·내부 오류 시 발생하며 done 없이 스트림이 끝난다. BE는 비스트리밍 `/chat`과 동일하게 이 메시지를 답변으로 저장한다.
- 답을 찾지 못한 경우(NO_RESULT): `token` 없이 `done`만(`sources` 빈 배열) 보낸다. BE는 누적 답변이 비어 있으면 워키 질문 등록 유도 메시지로 대체한다.
- AI 서버 연결 자체가 끊기는 전송 오류는 BE가 감지해 Fallback 안내 메시지로 저장·전달한다.

챗봇 세션 컨텍스트 규칙:

- `sessionContext`는 `messageId`, `senderType`, `content`를 포함하며 `USER`, `ASSISTANT`만 허용한다.
- BE는 현재 질문을 중복 포함하지 않고 같은 세션의 최근 메시지를 오래된 순서로 전달한다.
- AI 출처의 `sourceType`, `sourceId`, `chunkIndex`를 사용해 BE의 인용 대상을 식별한다.

동기화 규칙:

- `DEPT_RR`의 `sourceId`는 `departmentId`와 같고, `ROUTING_CASE`의 `sourceId`는 승인 사례 고유 ID다.
- BE는 원문과 `ai_sync_jobs`를 같은 트랜잭션에 저장하고 워커에서 AI API를 호출한다.
- AI 호출 실패를 업무 데이터 저장 성공으로 숨기지 않으며, 비동기 작업은 `FAILED`와 실패 사유를 남겨 재시도한다.


## 6. Worki API

담당: 민정기


| Method | Path                                    | 설명     | 인증  |
| ------ | --------------------------------------- | ------ | --- |
| GET    | `/worki/questions`                      | 질문 목록  | USER  |
| POST   | `/worki/questions`                      | 질문 등록  | USER  |
| GET    | `/worki/questions/{questionId}`         | 질문 상세  | USER  |
| PATCH  | `/worki/questions/{questionId}`         | 질문 수정  | USER  |
| POST   | `/worki/questions/{questionId}/answers` | 답변 등록  | USER  |
| POST   | `/worki/answers/{answerId}/accept`      | 답변 채택  | USER  |
| POST   | `/worki/questions/{questionId}/like`    | 좋아요    | USER  |
| DELETE | `/worki/questions/{questionId}/like`    | 좋아요 취소 | USER  |


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


#### 현재 구현된 Storage API

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/api/v1/storage/presigned-upload` | 업로드 URL, `objectKey`, `publicUrl` 발급 | 필요 |
| GET | `/api/v1/storage/presigned-download?objectKey=...` | 다운로드 URL 발급 | 필요 |
| DELETE | `/api/v1/storage?objectKey=...` | object 삭제 | 필요 |


## 8. FAQ API

담당: 민정기


| Method | Path                   | 설명        | 인증  |
| ------ | ---------------------- | --------- | --- |
| GET    | `/faq/worki/popular`   | 인기 워키     | USER  |
| GET    | `/faq/manuals/popular` | 인기 매뉴얼    | USER  |
| GET    | `/faq/manuals/recent`  | 최근 등록 매뉴얼 | USER  |


## 8-1. Search API (통합검색)

담당: 민정기

| Method | Path                              | 설명                          | 인증   |
| ------ | --------------------------------- | --------------------------- | ---- |
| GET    | `/search`                         | 통합검색(워키+매뉴얼, 도메인별 분리)        | USER |
| GET    | `/search/worki`                   | 워키 질문 검색(Elasticsearch)      | USER |
| GET    | `/search/worki/autocomplete`      | 워키 검색어 자동완성(DB)             | USER |
| GET    | `/search/manuals`                 | 매뉴얼 검색(DB, 발행본만)            | USER |
| POST   | `/search/worki/reindex`           | 워키 ES 전체 재색인(관리자용 임시)        | 필요   |

- `keyword`: 필수, 2~100자.
- 페이지 파라미터: `page`(0-base), `size`. 응답은 공통 `PageResponse`(§2.4).
- **통합검색**(`GET /search?keyword=&size=`): 워키·매뉴얼을 각각 미리보기 크기(`size`, 기본 5)만큼 page 0으로 조회해 **도메인별로 분리**해 반환. 각 도메인 `pageInfo.totalElements` 로 전체 건수 표시. 워키(ES) 조회가 실패해도 매뉴얼(DB) 결과는 반환(워키는 빈 결과).
- **매뉴얼 검색**: Elasticsearch 색인 없이 DB에서 `status=PUBLISHED` 매뉴얼의 제목·본문을 검색(ADR-009 참고). 미발행/삭제 매뉴얼은 노출하지 않는다.

```jsonc
// GET /api/v1/search?keyword=휴가&size=5
{
  "worki": {
    "content": [
      { "questionId": 7, "title": "휴가 신청은 어디서?", "status": "WAITING", "viewCount": 12, "createdAt": "2026-06-14T10:00:00" }
    ],
    "pageInfo": { "page": 1, "size": 5, "totalElements": 3, "totalPages": 1, "hasNext": false, "hasPrevious": false }
  },
  "manuals": {
    "content": [
      { "manualId": 2, "title": "연차 휴가 규정", "status": "PUBLISHED", "departmentId": 1, "version": "1.0", "createdAt": "2026-06-10T09:00:00" }
    ],
    "pageInfo": { "page": 1, "size": 5, "totalElements": 1, "totalPages": 1, "hasNext": false, "hasPrevious": false }
  }
}
```


## 9. Notification API

담당: 민정기


| Method | Path                                   | 설명        | 인증  |
| ------ | -------------------------------------- | --------- | --- |
| GET    | `/notifications`                       | 알림 목록     | USER  |
| GET    | `/notifications/unread-count`          | 미읽은 알림 갯수 | USER  |
| PATCH  | `/notifications/{notificationId}/read` | 개별 읽음     | USER  |
| PATCH  | `/notifications/read-all`              | 모두 읽음     | USER  |
| DELETE | `/notifications/{notificationId}`      | 알림 삭제     | USER  |


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


## 10. Point API

담당: 이슬이


| Method | Path                  | 설명           | 인증              |
| ------ |-----------------------|--------------|-----------------|
| GET    | `/me/points`          | 현재 보유 포인트 조회 | Access Token 필요 |
| GET    | `/me/point-histories` | 포인트 변동 내역 조회 | Access Token 필요 |


## 11. ESG Metrics & 리더보드 API

담당: 김가영, 이슬이


| Method | Path                 | 설명            | 인증                       |
| ------ |----------------------|---------------| ------------------------ |
| GET    | `/esg/metrics/me`    | 내 ESG/기여 지표   | 필요                       |
| GET    | `/admin/esg/metrics` | 관리자 ESG 운영 지표 | TEAM_ADMIN, SYSTEM_ADMIN |
| GET    | `/leaderboard`       | 리더보드 조회       | Access Token 필요                       |


## 12. Admin API

담당: 김가영

팀 대시보드

| Method | Path                                      | 설명                         | 인증 |
| ------ | ----------------------------------------- | ---------------------------- | ---- |
| GET    | `/team/tickets/summary`                   | 우리 부서 티켓 요약 조회       | USER, TEAM_ADMIN | 추후 팀 어드민으로만 박기
| GET    | `/team/tickets`                           | 우리 부서 티켓 목록 조회       | USER, TEAM_ADMIN |
| GET    | `/team/tickets/{ticketId}`                | 우리 부서 티켓 상세 조회       | USER, TEAM_ADMIN |
| POST   | `/tickets/{ticketId}/answers`             | 티켓 답변 등록 및 완료 처리    | USER, TEAM_ADMIN |
| GET    | `/tickets/{ticketId}/answers/latest`      | 티켓 최신 답변 조회(완료티켓 확인용)     | USER, TEAM_ADMIN |

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
| GET    | `/admin/points`                                 | 전체 사용자 포인트 목록 조회               | SYSTEM_ADMIN |
| GET    | `/admin/points/search`                          | 사번으로 사용자 포인트 조회               | SYSTEM_ADMIN |
| PATCH  | `/admin/points/{employeeId}/deduct`             | 포인트 차감                        | SYSTEM_ADMIN |
| DELETE | `/admin/worki/questions/{questionId}`           | 워키 게시글 관리자 삭제 및 작성자 포인트 차감    | SYSTEM_ADMIN |
| GET    | `/admin/departments`                            | 관리자 부서 목록 조회                  | SYSTEM_ADMIN |
| POST   | `/admin/departments`                            | 부서 등록                         | SYSTEM_ADMIN |
| PATCH  | `/admin/departments/{departmentId}`             | 부서 정보 수정                      | SYSTEM_ADMIN |
| DELETE | `/admin/departments/{departmentId}`             | 부서 삭제                         | SYSTEM_ADMIN |
| PATCH  | `/admin/departments/routing-prompt/instruction` | 부서 라우팅 프롬프트                   | SYSTEM_ADMIN |
| PATCH  | `/admin/departments/{departmentId}/routing-prompt` | 부서별 R&R 직접 수정                 | SYSTEM_ADMIN |
| GET    | `/admin/users`                                  | 전체 사용자 목록 조회                    | SYSTEM_ADMIN |
| GET    | `/admin/users/search`                           | 사번으로 사용자 조회                   | SYSTEM_ADMIN |
| PATCH  | `/admin/users/{userId}/status`                  | 사용자 활성화/비활성화 변경               | SYSTEM_ADMIN |
| GET    | `/admin/manuals`                                | 매뉴얼 목록 조회                     | SYSTEM_ADMIN |
| POST   | `/admin/manuals`                                | 매뉴얼 등록 (본문 직접 입력)             | SYSTEM_ADMIN |
| POST   | `/admin/manuals/pdf`                            | 매뉴얼 등록 (PDF 업로드)              | SYSTEM_ADMIN |
| GET    | `/admin/manuals/{manualId}`                     | 매뉴얼 상세 조회                     | SYSTEM_ADMIN |
| GET    | `/admin/manuals/{manualId}/versions`            | 매뉴얼 버전 이력 및 본문 diff 조회       | SYSTEM_ADMIN |
| PATCH  | `/admin/manuals/{manualId}`                     | 매뉴얼 수정 및 신규 버전 등록             | SYSTEM_ADMIN |
| PATCH  | `/admin/manuals/{manualId}/pdf`                 | 매뉴얼 본문 PDF 교체                 | SYSTEM_ADMIN |
| POST   | `/admin/manuals/{manualId}/versions/{versionId}/resummarize` | 버전 변경사항 AI 요약 재요청        | SYSTEM_ADMIN |
| DELETE | `/admin/manuals/{manualId}`                     | 매뉴얼 삭제                        | SYSTEM_ADMIN |

#### 매뉴얼 버전 응답 (ManualVersionResponse) — AI 요약 관련 필드

- `updateReasonLabel` (string): `updateReason` 코드를 사용자 문장으로 변환한 값
- `changeSummary` (string|null): AI가 생성한 본문 변경 한 줄 요약 (비동기 생성, 미생성 시 null)
- `summaryStatus` (string): 해당 버전 요약 잡 상태 — `NONE | PENDING | PROCESSING | SYNCED | FAILED`

FE 표시 우선순위: `changeSummary || updateReasonLabel || (contentDiff 가공)`

`POST .../versions/{versionId}/resummarize`
- 해당 버전의 AI 요약을 다시 큐잉한다. `contentDiff`가 없는 버전은 400.
- 응답: 200 OK (body 없음)
- FE 재요약 버튼 노출 조건: `contentDiff 있음 && changeSummary 없음 && summaryStatus in (NONE, FAILED)`
| GET    | `/admin/flash-chat/policy`                      | TTL, 쿨다운, 금지어 정책 조회           | SYSTEM_ADMIN |
| PATCH  | `/admin/flash-chat/policy`                      | TTL, 쿨다운, 금지어 정책 일괄 변경        | SYSTEM_ADMIN |
| DELETE | `/admin/flash-chat/messages/{messageId}`        | Flash Chat 메시지 강제 삭제          | SYSTEM_ADMIN |
| GET    | `/admin/direct-data`             | 수기 지식 목록 조회                        | SYSTEM_ADMIN   |
| GET    | `/admin/direct-data/{id}`        | 수기 지식 상세 조회                        | SYSTEM_ADMIN   |
| POST   | `/admin/direct-data`             | 수기 지식 등록                             | SYSTEM_ADMIN   |
| PUT    | `/admin/direct-data/{id}`        | 수기 지식 수정                             | SYSTEM_ADMIN   |
| DELETE | `/admin/direct-data/{id}`        | 수기 지식 삭제                             | SYSTEM_ADMIN   |





## 13.매뉴얼 (Manual)

담당: 김가영

| Method | Path                            | 설명                | 인증           |
| ------ | ------------------------------- | ----------------- | ------------ |
| GET    | `/manuals`                | 매뉴얼 목록 조회         | USER |
| GET    | `/manuals/{manualId}`     | 매뉴얼 상세 조회         | USER |


## 14.수기지식 게시판 

담당: 김가영

| Method | Path                            | 설명                | 인증           |
| ------ | ------------------------------- | ----------------- | ------------ |
| GET    | `/direct-data`                   | 활성 수기 지식 목록 조회                     | USER             |
| GET    | `/direct-data/{id}`              | 활성 수기 지식 상세 조회                     | USER             |


## 15. 지식화 게시판

담당: 김가영

| Method | Path                            | 설명                | 인증           |
| ------ | ------------------------------- | ----------------- | ------------ |
| GET    | `/knowledge-data`                   | 지식화 게시판 목록 조회                     | USER             |
| GET    | `/knowledge-data/{id}`              | 지식화 게시판 상세 조회                     | USER             |

## 16. 미정 항목


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
