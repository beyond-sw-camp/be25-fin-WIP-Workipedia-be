# API Contract

> 문서 유형: API Contract
> 상태: Draft
> 정본 위치: `docs/api/api-contract.md`
> 기준: `docs/api/openapi.json` + Controller 매핑 스캔
> 버전: v1.1
> 최종 수정: 2026-06-26

## 1. 목적

프론트엔드와 백엔드가 같은 API 주소를 기준으로 개발하기 위한 API 주소 목록이다.

## 2. 공통 규칙

### 2.1 Base URL

| 환경 | Base URL |
| --- | --- |
| local | `http://localhost:8080/api/v1` |
| dev/staging | 미정 |
| production | 미정 |

### 2.2 인증

인증이 필요한 API는 JWT Access Token을 사용한다.

```http
Authorization: Bearer <accessToken>
```

## 3. API 주소 목록

- 아래 Path는 `/api/v1` 이후 경로만 적었다.
- API 주소 수: `134`
- `비고`가 `Controller`인 항목은 Controller에는 구현되어 있지만 현재 `docs/api/openapi.json`에는 없는 주소다.

### 3.1 Auth

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `POST` | `/auth/login` | 로그인 | - |
| `POST` | `/auth/logout` | 로그아웃 | - |
| `PATCH` | `/auth/password-reset` | 비밀번호 재설정 | - |
| `POST` | `/auth/password-reset/code` | 비밀번호 재설정 인증코드 발송 | - |
| `POST` | `/auth/password-reset/code/verify` | 비밀번호 재설정 인증코드 확인 | - |
| `POST` | `/auth/signup` | 회원가입 | - |
| `POST` | `/auth/signup/code` | 회원가입 인증코드 발송 | - |
| `POST` | `/auth/signup/code/verify` | 회원가입 인증코드 확인 | - |
| `POST` | `/auth/token/refresh` | 토큰 재발급 | - |

### 3.2 Departments

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `GET` | `/departments` | 부서 목록 조회 | - |

### 3.3 Chatbot

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `GET` | `/chatbot/sessions` | 내 챗봇 세션 목록 조회 | - |
| `POST` | `/chatbot/sessions` | 챗봇 세션 생성 | - |
| `GET` | `/chatbot/sessions/{sessionId}/messages` | 세션 메시지 목록 조회 | - |
| `POST` | `/chatbot/sessions/{sessionId}/messages` | 질문 전송 및 AI 답변 생성 | - |
| `POST` | `/chatbot/sessions/{sessionId}/messages/stream` | 챗봇 질문 전송 및 AI 답변 스트리밍 | Controller |

### 3.4 Worki

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `DELETE` | `/admin/worki/questions/{questionId}` | 관리자 워키 질문 삭제 및 포인트 차감 | - |
| `POST` | `/worki/answers/{answerId}/accept` | 워키 답변 채택 | - |
| `GET` | `/worki/questions` | 워키 질문 목록 조회 | - |
| `POST` | `/worki/questions` | 워키 질문 등록 | - |
| `GET` | `/worki/questions/{questionId}` | 워키 질문 상세 조회 | - |
| `PATCH` | `/worki/questions/{questionId}` | 워키 질문 수정 | - |
| `POST` | `/worki/questions/{questionId}/answers` | 워키 답변 등록 | - |
| `DELETE` | `/worki/questions/{questionId}/like` | 워키 질문 좋아요 취소 | - |
| `POST` | `/worki/questions/{questionId}/like` | 워키 질문 좋아요 | - |
| `POST` | `/worki/questions/bulk` | 워키 질문 일괄 등록 (테스트용) | - |

### 3.5 Ticket

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `GET` | `/team/tickets` | 팀 티켓 목록 조회 | Controller |
| `GET` | `/team/tickets/{ticketId}` | 팀 티켓 상세 조회 | Controller |
| `POST` | `/team/tickets/{ticketId}/transfer` | 팀 티켓 공통 접수로 이관 요청 | Controller |
| `GET` | `/team/tickets/summary` | 팀 티켓 요약 조회 | Controller |
| `GET` | `/tickets` | 내 팀 티켓 목록 조회 | - |
| `POST` | `/tickets` | 티켓 생성 | - |
| `GET` | `/tickets/{ticketId}` | 티켓 상세 조회 | - |
| `POST` | `/tickets/{ticketId}/answers` | 티켓 답변 등록 | Controller |
| `GET` | `/tickets/{ticketId}/answers/latest` | 티켓 최신 답변 조회 | Controller |
| `PATCH` | `/tickets/{ticketId}/assignee` | 티켓 담당자 배정 | - |
| `PATCH` | `/tickets/{ticketId}/status` | 티켓 상태 변경 | - |

### 3.6 Team Admin Dashboard

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `GET` | `/admin/team/dashboard/chatbot-ticket-trend` | 팀 챗봇 티켓 추이 조회 | Controller |
| `GET` | `/admin/team/dashboard/knowledge-trend` | 팀 지식화 추이 조회 | Controller |
| `GET` | `/admin/team/dashboard/summary` | 팀 관리자 대시보드 요약 조회 | Controller |

### 3.7 Team Admin Knowledge Data

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `GET` | `/admin/team/knowledge-data` | 승인된 팀 지식 목록 조회 | Controller |
| `DELETE` | `/admin/team/knowledge-data/{knowledgeDataId}` | 팀 지식 삭제 | Controller |
| `PATCH` | `/admin/team/knowledge-data/{knowledgeDataId}` | 팀 지식 수정 | Controller |
| `GET` | `/admin/team/knowledge-data/candidates` | 지식화 승인 후보 티켓 목록 조회 | Controller |
| `POST` | `/admin/team/knowledge-data/tickets/{ticketId}/approve` | 티켓 답변을 지식으로 승인 | Controller |
| `POST` | `/admin/team/knowledge-data/tickets/{ticketId}/reject` | 티켓 답변 지식화 반려 | Controller |

### 3.8 Admin Dashboard

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `GET` | `/admin/dashboard/department-auto-assignment-rate` | 부서별 자동 배정률 조회 | Controller |
| `GET` | `/admin/dashboard/department-ticket-status` | 부서별 티켓 처리 현황 조회 | Controller |
| `GET` | `/admin/dashboard/monthly-auto-assignment-rate` | 월별 자동 배정률 통계 조회 | Controller |
| `GET` | `/admin/dashboard/monthly-ticket-trend` | 월별 티켓 생성 추이 조회 | Controller |

### 3.9 Admin Common Queue

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `GET` | `/admin/common-queue/tickets` | 공통 접수 티켓 목록 조회 | Controller |
| `PATCH` | `/admin/common-queue/tickets/{ticketId}/department` | 공통 접수 티켓 담당 부서 배정 | Controller |

### 3.10 Admin AI

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `GET` | `/admin/ai-prompt-settings` | AI 프롬프트 설정 조회 | Controller |
| `PUT` | `/admin/ai-prompt-settings` | AI 프롬프트 설정 수정 | Controller |
| `GET` | `/admin/ai-sync-jobs` | AI 동기화 작업 목록 조회 | Controller |
| `POST` | `/admin/ai-sync-jobs/{jobId}/retry` | 특정 AI 동기화 실패 작업 재시도 | Controller |
| `POST` | `/admin/ai-sync-jobs/cleanup-worki` | 오래된 워키 AI 동기화 데이터 정리 실행 | Controller |
| `GET` | `/admin/ai-sync-jobs/cleanup-worki/logs` | 워키 AI 동기화 정리 로그 조회 | Controller |
| `POST` | `/admin/ai-sync-jobs/retry-all` | 실패한 AI 동기화 작업 전체 재시도 | Controller |
| `GET` | `/admin/ai-sync-jobs/settings` | AI 동기화 설정 조회 | Controller |
| `PUT` | `/admin/ai-sync-jobs/settings` | AI 동기화 설정 수정 | Controller |
| `GET` | `/admin/ai-sync-jobs/stats` | AI 동기화 작업 통계 조회 (`sourceTypes` 쿼리로 스코프 필터, 예: `KNOWLEDGE_DATA,MANUAL_KNOWLEDGE`) | Controller |
| `POST` | `/admin/ai-sync-jobs/run-now` | 지식 데이터 대기 작업 즉시 실행 (비동기 드레인, 응답 `{queued}`) | Controller |
| `POST` | `/admin/ai-sync-jobs/resync-knowledge` | 지식 데이터 전체 재동기화 (활성 원본 UPSERT, 응답 `{enqueued, skipped}`) | Controller |
| `GET` | `/admin/ai-tools` | AI Tool 목록 조회 | Controller |
| `POST` | `/admin/ai-tools` | AI Tool 등록 (`sideEffectType`: `READ_ONLY`/`MUTATING`) | Controller |
| `PATCH` | `/admin/ai-tools/{aiToolId}` | AI Tool 설정 및 `sideEffectType` 수정 | Controller |
| `POST` | `/admin/ai-tools/{aiToolId}/health-check` | 등록된 READ_ONLY AI Tool 상태 점검(MUTATING은 실행 차단) | Controller |
| `POST` | `/admin/ai-tools/health-check` | 저장 전 READ_ONLY AI Tool 초안 상태 점검(`sideEffectType` 전달) | Controller |

### 3.11 Admin Departments

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `GET` | `/admin/departments` | 관리자 부서 목록 조회 | - |
| `POST` | `/admin/departments` | 부서 등록 | - |
| `DELETE` | `/admin/departments/{departmentId}` | 부서 삭제 | - |
| `PATCH` | `/admin/departments/{departmentId}` | 부서 수정 | - |
| `PATCH` | `/admin/departments/{departmentId}/routing-prompt` | 특정 부서 라우팅 프롬프트 직접 수정 | Controller |
| `PATCH` | `/admin/departments/routing-prompt/instruction` | 부서 라우팅 프롬프트 일괄 편집 | - |
| `POST` | `/admin/departments/sync/fetch` | ERP 부서 API URL 조회(BE 프록시, CORS 회피) → {columns, rows} | Controller |
| `POST` | `/admin/departments/sync/preview` | ERP 부서 목록 vs 운영 부서 diff 미리보기 | Controller |
| `POST` | `/admin/departments/sync/apply` | 검토 완료 변경 반영(부서 upsert/soft-delete + 사원 재배치 + R&R + ai_sync) | Controller |

### 3.12 Admin Manuals

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `GET` | `/admin/manuals` | 관리자 매뉴얼 목록 조회 | - |
| `POST` | `/admin/manuals` | 매뉴얼 등록 | - |
| `DELETE` | `/admin/manuals/{manualId}` | 매뉴얼 삭제 | - |
| `GET` | `/admin/manuals/{manualId}` | 관리자 매뉴얼 상세 조회 | - |
| `PATCH` | `/admin/manuals/{manualId}` | 매뉴얼 수정 | - |
| `PATCH` | `/admin/manuals/{manualId}/pdf` | PDF 매뉴얼 수정 | - |
| `GET` | `/admin/manuals/{manualId}/versions` | 매뉴얼 버전 목록 조회 | Controller |
| `POST` | `/admin/manuals/{manualId}/versions/{versionId}/resummarize` | 특정 매뉴얼 버전 요약 재생성 | Controller |
| `POST` | `/admin/manuals/pdf` | PDF 매뉴얼 등록 | - |

### 3.13 Admin Users

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `GET` | `/admin/users` | 관리자 사용자 목록 조회 | Controller |
| `PATCH` | `/admin/users/{userId}/role` | 사용자 권한 변경 또는 팀 관리자 승격 | Controller |
| `PATCH` | `/admin/users/{userId}/status` | 사용자 상태 변경 | - |
| `GET` | `/admin/users/search` | 사번으로 사용자 조회 | - |

### 3.14 Admin Settings

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `GET` | `/admin/settings/summary` | 관리자 설정 요약 조회 | - |

### 3.15 Point

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `GET` | `/admin/points` | 전체 사용자 포인트 목록 조회 | Controller |
| `PATCH` | `/admin/points/{employeeId}/deduct` | 사용자 포인트 차감 | - |
| `GET` | `/admin/points/search` | 사번으로 사용자 포인트 조회 | - |
| `GET` | `/me/point-histories` | 내 포인트 변동 내역 조회 | - |
| `GET` | `/me/points` | 내 포인트 조회 | - |

### 3.16 ESG

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `GET` | `/admin/esg/infra` | 인프라 ESG 효율 분석 요약 조회 | Controller |
| `GET` | `/esg/leaderboard` | ESG 리더보드 조회 | - |
| `GET` | `/esg/me` | 내 ESG 정보 조회 | - |

### 3.17 Manuals

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `GET` | `/manuals` | 게시된 매뉴얼 목록 조회 | - |
| `GET` | `/manuals/{manualId}` | 게시된 매뉴얼 상세 조회 | - |

### 3.18 Knowledge Data

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `DELETE` | `/admin/knowledge-data/{knowledgeDataId}` | 시스템 관리자 승인 지식 삭제 | Controller |
| `GET` | `/knowledge-data` | 승인 지식 목록 조회 | Controller |
| `DELETE` | `/knowledge-data/{knowledgeDataId}` | 승인 지식 삭제 | Controller |
| `GET` | `/knowledge-data/{knowledgeDataId}` | 승인 지식 상세 조회 | Controller |

### 3.19 Direct Data

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `GET` | `/admin/direct-data` | 관리자 직접 입력 지식 목록 조회 | Controller |
| `POST` | `/admin/direct-data` | 관리자 직접 입력 지식 등록 | Controller |
| `DELETE` | `/admin/direct-data/{directDataId}` | 관리자 직접 입력 지식 삭제 | Controller |
| `GET` | `/admin/direct-data/{directDataId}` | 관리자 직접 입력 지식 상세 조회 | Controller |
| `PUT` | `/admin/direct-data/{directDataId}` | 관리자 직접 입력 지식 수정 | Controller |
| `GET` | `/direct-data` | 직접 입력 지식 목록 조회 | Controller |
| `GET` | `/direct-data/{directDataId}` | 직접 입력 지식 상세 조회 | Controller |

### 3.20 FAQ

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `GET` | `/faq/manuals/popular` | 인기 매뉴얼 조회 | - |
| `GET` | `/faq/manuals/recent` | 최신 매뉴얼 조회 | - |
| `GET` | `/faq/worki/popular` | 인기 워키 조회 | - |

### 3.21 Notifications

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `GET` | `/notifications` | 알림 목록 조회 | - |
| `DELETE` | `/notifications/{notificationId}` | 알림 삭제 | - |
| `PATCH` | `/notifications/{notificationId}/read` | 알림 읽음 처리 | - |
| `PATCH` | `/notifications/read-all` | 모든 알림 읽음 처리 | - |
| `GET` | `/notifications/unread-count` | 읽지 않은 알림 수 조회 | - |

### 3.22 My Page

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `PATCH` | `/me/notification-settings` | 알림 설정 변경 | - |
| `GET` | `/me/profile` | 마이페이지 요약 조회 | - |
| `GET` | `/me/tickets` | 내 티켓 목록 조회 | - |
| `GET` | `/me/tickets/{ticketId}` | 내 티켓 상세 조회 | - |

### 3.23 Search

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `GET` | `/search` | 통합 검색 | Controller |
| `GET` | `/search/manuals` | 매뉴얼 검색 | Controller |
| `GET` | `/search/worki` | 워키 질문 검색 | - |
| `GET` | `/search/worki/autocomplete` | 워키 검색어 자동완성 | - |
| `POST` | `/search/worki/reindex` | 워키 검색 색인 전체 재생성 | - |

### 3.24 Storage

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `DELETE` | `/storage` | 스토리지 객체 삭제 | - |
| `GET` | `/storage/presigned-download` | 다운로드용 Presigned URL 발급 | - |
| `POST` | `/storage/presigned-upload` | 업로드용 Presigned URL 발급 | - |

### 3.25 Flash Chat

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `DELETE` | `/admin/flash-chat/messages/{messageId}` | FlashChat 메시지 강제 삭제 | - |
| `GET` | `/admin/flash-chat/policy` | FlashChat 정책 조회 | - |
| `PATCH` | `/admin/flash-chat/policy` | FlashChat 정책 수정 | - |
| `GET` | `/flash-chat/messages` | FlashChat 최근 메시지 조회 | - |

### 3.26 Leaderboard

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `GET` | `/leaderboard` | 리더보드 조회 | Controller |

### 3.27 Internal AI Tools

| Method | Path | 설명 | 비고 |
| --- | --- | --- | --- |
| `POST` | `/internal/ai-tools/{aiToolId}/execute` | READ_ONLY AI Tool 내부 실행 (`callerEmployeeId` 전달, SELF_ONLY 강제 주입) | Controller |
| `GET` | `/internal/ai-tools/active` | 활성·승인 READ_ONLY AI Tool 목록 내부 조회(5분 캐시) | Controller |

## 4. 관리 메모

- API가 추가/삭제되면 이 목록을 같이 갱신한다.
- 요청/응답 DTO 상세는 코드의 Request/Response DTO와 `openapi.json`을 기준으로 확인한다.
- 내부용 API는 프론트 공개 API와 구분해서 표시한다.
