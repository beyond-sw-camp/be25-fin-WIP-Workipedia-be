# DB Migration Guide

> 문서 유형: DB Migration Guide
> 상태: Draft
> 정본 위치: `docs/database/db-migration-guide.md`
> 관련 문서: `docs/reference/trd.md`, `docs/api/api-contract.md`
> 버전: v0.1
> 최종 수정: 2026-05-28

## 1. 목적

DB 스키마 변경을 Flyway migration으로 관리하기 위한 규칙이다.

운영 DB 또는 공유 DB에 수동 DDL을 직접 반영하지 않는다. 모든 스키마 변경은 migration 파일로 남긴다.

## 2. 기본 위치

```text
src/main/resources/db/migration/
```

## 3. 파일명 규칙

```text
V{번호}__{설명}.sql
```

예시:

```text
V1__create_departments_and_users.sql
V2__create_worki.sql
V3__create_chatbot.sql
V4__create_tickets.sql
V5__create_points_and_notifications.sql
V6__create_admin_logs.sql
V7__create_manuals_and_chunks.sql
V8__create_badges_and_esg_metrics.sql
```

## 4. 권장 생성 순서

| 순서 | 파일 | 주요 담당 | 포함 테이블 |
|---|---|---|---|
| 1 | `V1__create_departments_and_users.sql` | 이슬이 | `departments`, `users` |
| 2 | `V2__create_worki.sql` | 민정기 | `worki_questions`, `worki_answers`, `reactions` |
| 3 | `V3__create_chatbot.sql` | 이슬이, 김진혁 | `chatbot_sessions`, `chatbot_messages` |
| 4 | `V4__create_tickets.sql` | 김진혁 | `tickets`, `ticket_answers`, `ticket_transfer_requests` |
| 5 | `V5__create_points_and_notifications.sql` | 김가영, 민정기 | `point_history`, `notifications` |
| 6 | `V6__create_admin_logs.sql` | 김가영 | `admin_logs` |
| 7 | `V7__create_manuals_and_chunks.sql` | 김진혁 | `manuals`, `manual_chunks`, `worki_chunks` |
| 8 | `V8__create_badges_and_esg_metrics.sql` | 김가영 | `badges`, `user_badges`, `esg_metric_snapshots` |

## 5. 공통 컬럼 규칙

가능하면 모든 주요 테이블에 아래 컬럼을 둔다.

```sql
created_at DATETIME NOT NULL,
updated_at DATETIME NOT NULL,
deleted_at DATETIME NULL
```

삭제 정책이 필요한 테이블은 hard delete 대신 `deleted_at` 기반 soft delete를 우선한다.

## 6. 네이밍 규칙

| 대상 | 규칙 | 예시 |
|---|---|---|
| 테이블 | snake_case, 복수형 우선 | `worki_questions` |
| PK | `{단수}_id` | `question_id` |
| FK | 참조 대상 PK 이름 사용 | `user_id`, `department_id` |
| 상태 | VARCHAR + CHECK 또는 enum-like string | `WAITING`, `ANSWERED` |
| 시간 | `_at` suffix | `created_at`, `accepted_at` |

## 7. 상태값 초안

### worki_questions.status

| 값 | 의미 |
|---|---|
| `WAITING` | 답변 대기 |
| `IN_PROGRESS` | 답변 진행 |
| `ANSWERED` | 채택 완료 |
| `TICKETED` | 티켓 전환 |
| `DELETED` | 관리자 삭제 처리 |

### tickets.status

| 값 | 의미 |
|---|---|
| `PENDING` | 처리 대기 |
| `TRANSFER_REQUESTED` | 이관 요청 |
| `TRANSFERRED` | 이관 완료 |
| `COMPLETED` | 처리 완료 |
| `REJECTED` | 반려 |
| `DELETED` | 삭제 처리 |

### ticket_transfer_requests.status

| 값 | 의미 |
|---|---|
| `REQUESTED` | 이관 요청됨 |
| `APPROVED` | 이관 승인 |
| `REJECTED` | 이관 반려 |

### notifications.type

| 값 | 의미 |
|---|---|
| `WORKI_ANSWER_CREATED` | 워키 답변 등록 |
| `WORKI_ANSWER_ACCEPTED` | 답변 채택 |
| `TICKET_STATUS_CHANGED` | 티켓 상태 변경 |
| `TICKET_TRANSFER_REQUESTED` | 티켓 이관 요청 |
| `POINT_EARNED` | 포인트 획득 |
| `BADGE_EARNED` | 뱃지 획득 |

### badges.code

| 값 | 의미 |
|---|---|
| `FIRST_QUESTION` | 첫 질문 |
| `FIRST_ACCEPTED_ANSWER` | 첫 채택 답변 |
| `ANSWER_HELPER` | 답변 5개 이상 |

## 8. 담당자별 주의사항

| 담당 | 주의사항 |
|---|---|
| 이슬이 | `users.role`은 `USER`, `ADMIN`만 허용 |
| 민정기 | 워키 질문/답변은 USER hard delete 금지 |
| 김진혁 | `chatbot_messages.references`는 JSON으로 남기고, 티켓 이관 이력을 보존 |
| 김가영 | 모든 관리자 작업은 `admin_logs`에 기록하고, 포인트/뱃지/ESG 지표를 조회 가능하게 설계 |
| 황희수 | 프론트에서 의존하는 enum/status 값 변경 시 즉시 공유 |

## 9. PR 체크리스트

- [ ] migration 파일명이 순서대로 되어 있다.
- [ ] 기존 migration 파일을 수정하지 않았다.
- [ ] 새 테이블에 PK가 있다.
- [ ] 필요한 FK와 index가 있다.
- [ ] soft delete 대상에는 `deleted_at`이 있다.
- [ ] 상태값이 API 계약서와 맞다.
- [ ] 로컬 빈 DB에서 migration이 성공한다.
