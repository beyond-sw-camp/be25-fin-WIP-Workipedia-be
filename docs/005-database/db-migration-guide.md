# DB Migration Guide

> 문서 유형: DB Migration Guide
> 상태: Draft
> 정본 위치: `docs/005-database/db-migration-guide.md`
> 관련 문서: `docs/001-reference/trd.md`, `docs/004-api/api-contract.md`
> 버전: v0.2
> 최종 수정: 2026-05-31

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

## 4. Migration 불변 원칙

한 번 커밋되었거나 팀원에게 공유된 migration 파일은 수정하지 않는다.
Flyway는 이미 적용된 migration의 checksum을 관리하므로, 기존 `V1`~`V8` 파일을 나중에 수정하면 로컬/공유 DB의 migration 이력과 파일 내용이 달라져 오류가 발생할 수 있다.

스키마 변경은 항상 다음 번호의 새 파일로 추가한다.

```text
이미 공유된 파일 수정 금지:
V1__create_departments_and_users.sql

추가 변경은 새 파일로 작성:
V9__add_ticket_routing_tables.sql
V10__alter_manual_status.sql
```

예외적으로 기존 migration을 수정할 수 있는 경우는 아래 두 가지뿐이다.

| 상황 | 허용 여부 |
|---|---|
| 아직 커밋하지 않았고 아무도 적용하지 않은 개인 작업 중 migration | 수정 가능 |
| 이미 PR에 올라갔거나 dev에 merge되었거나 팀원이 pull 받은 migration | 수정 금지, 다음 번호로 추가 |

초기 개발 단계라도 팀원이 함께 작업하는 저장소에서는 `V1` 하나에 모든 테이블을 계속 몰아넣지 않는다. 테이블을 도메인별 migration으로 나누면 리뷰 범위가 작아지고, 담당자별 변경 이력과 충돌 원인을 추적하기 쉽다.

## 5. 권장 생성 순서

| 순서 | 파일 | 주요 담당 | 포함 테이블 |
|---|---|---|---|
| 1 | `V1__create_departments_and_users.sql` | 이슬이 | `departments`, `users` |
| 2 | `V2__create_worki.sql` | 민정기 | `worki_questions`, `worki_answers`, `reactions` |
| 3 | `V3__create_chatbot.sql` | 이슬이, 김진혁 | `chatbot_sessions`, `chatbot_messages` |
| 4 | `V4__create_tickets.sql` | 김진혁 | `tickets`, `ticket_answers`, `ticket_transfer_requests`, `ticket_assignments`, `ticket_routing_logs`, `knowledge_candidates` |
| 5 | `V5__create_points_and_notifications.sql` | 김가영, 민정기 | `point_history`, `notifications` |
| 6 | `V6__create_admin_logs.sql` | 김가영 | `admin_logs` |
| 7 | `V7__create_manuals_and_chunks.sql` | 김진혁 | `manuals`, `manual_chunks`, `worki_chunks` |
| 8 | `V8__create_badges_and_esg_metrics.sql` | 김가영 | `badges`, `user_badges`, `esg_metric_snapshots` |
| 9 이후 | `V9__...sql` | 변경 담당자 | 기존 migration 수정 대신 추가/변경 DDL |

## 6. 공통 컬럼 규칙

가능하면 모든 주요 테이블에 아래 컬럼을 둔다.

```sql
created_at DATETIME NOT NULL,
updated_at DATETIME NOT NULL,
deleted_at DATETIME NULL
```

삭제 정책이 필요한 테이블은 hard delete 대신 `deleted_at` 기반 soft delete를 우선한다.

## 7. 네이밍 규칙

| 대상 | 규칙 | 예시 |
|---|---|---|
| 테이블 | snake_case, 복수형 우선 | `worki_questions` |
| PK | `{단수}_id` | `question_id` |
| FK | 참조 대상 PK 이름 사용 | `user_id`, `department_id` |
| 상태 | VARCHAR + CHECK 또는 enum-like string | `WAITING`, `ANSWERED` |
| 시간 | `_at` suffix | `created_at`, `accepted_at` |

## 8. 상태값 초안

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
| `RECEIVED` | 접수 완료 |
| `COMMON_QUEUE` | 공통 접수 큐 대기 |
| `ASSIGNED` | 담당 부서 배정 |
| `IN_PROGRESS` | 담당자 처리 중 |
| `COMPLETED` | 처리 완료 |
| `REJECTED` | 반려 |
| `DELETED` | 삭제 처리 |

### ticket_transfer_requests.status

| 값 | 의미 |
|---|---|
| `REQUESTED` | 이관 요청됨 |
| `ASSIGNED_FROM_QUEUE` | 공통 접수 큐에서 재배정 완료 |
| `REJECTED` | 이관 반려 |

### ticket_routing_logs.decision

| 값 | 의미 |
|---|---|
| `AUTO_ASSIGNED` | 신뢰도 기준을 통과해 자동 배정 |
| `ADMIN_REVIEW` | 후보 부서 추천 후 관리자 검토 필요 |
| `COMMON_QUEUE` | 공통 접수 큐 이동 |
| `NEED_MORE_INFO` | 사용자 추가 정보 필요 |

### knowledge_candidates.status

| 값 | 의미 |
|---|---|
| `DRAFT` | 초안 |
| `REVIEW_REQUESTED` | 팀 관리자 검수 요청 |
| `APPROVED` | 승인 |
| `REJECTED` | 반려 |
| `PUBLISHED` | 워키 반영 완료 |

### notifications.type

| 값 | 의미 |
|---|---|
| `WORKI_ANSWER_CREATED` | 워키 답변 등록 |
| `WORKI_ANSWER_ACCEPTED` | 답변 채택 |
| `TICKET_STATUS_CHANGED` | 티켓 상태 변경 |
| `TICKET_TRANSFER_REQUESTED` | TEAM_ADMIN의 티켓 이관 요청 |
| `COMMON_QUEUE_ASSIGNED` | 공통 접수 큐 티켓 재배정 |
| `POINT_EARNED` | 포인트 획득 |
| `BADGE_EARNED` | 뱃지 획득 |

### admin_logs.action_type

| 값 | 의미 |
|---|---|
| `USER_DEACTIVATE` | 사용자 비활성화 |
| `WORKI_READ` | 관리자 워키 조회 |
| `WORKI_UPDATE` | 관리자 워키 수정 |
| `WORKI_DELETE` | 관리자 워키 삭제 |
| `MANUAL_UPDATE` | 매뉴얼 수정 |
| `MANUAL_DELETE` | 매뉴얼 삭제 |
| `TICKET_ASSIGN` | 팀원 담당자 배정 |
| `TICKET_TRANSFER_REQUEST` | TEAM_ADMIN의 티켓 이관 요청 |
| `TICKET_ROUTE_OVERRIDE` | 자동 라우팅 결과 수동 변경 |
| `COMMON_QUEUE_ASSIGN` | 공통 접수 큐 티켓 부서 배정 |
| `KNOWLEDGE_REVIEW` | 지식화 후보 검수 |
| `KNOWLEDGE_PUBLISH` | 지식화 후보 워키 반영 |

### badges.code

| 값 | 의미 |
|---|---|
| `FIRST_QUESTION` | 첫 질문 |
| `FIRST_ACCEPTED_ANSWER` | 첫 채택 답변 |
| `ANSWER_HELPER` | 답변 5개 이상 |

## 9. 담당자별 주의사항

| 담당 | 주의사항 |
|---|---|
| 이슬이 | `users.role`은 `USER`, `TEAM_ADMIN`, `SYSTEM_ADMIN`만 허용 |
| 민정기 | 워키 질문/답변은 USER hard delete 금지 |
| 김진혁 | `chatbot_messages.references`는 JSON으로 남기고, 티켓 라우팅 점수/근거/이관 이력을 보존 |
| 김가영 | TEAM_ADMIN/SYSTEM_ADMIN 작업은 `admin_logs`에 기록하고, 팀 큐/공통 접수 큐/ESG 지표를 조회 가능하게 설계 |
| 황희수 | 프론트에서 의존하는 enum/status 값 변경 시 즉시 공유 |

## 10. PR 체크리스트

- [ ] migration 파일명이 순서대로 되어 있다.
- [ ] 기존 migration 파일을 수정하지 않았다.
- [ ] 이미 공유된 migration 변경이 필요하면 다음 번호 migration으로 작성했다.
- [ ] 새 테이블에 PK가 있다.
- [ ] 필요한 FK와 index가 있다.
- [ ] soft delete 대상에는 `deleted_at`이 있다.
- [ ] 상태값이 API 계약서와 맞다.
- [ ] 로컬 빈 DB에서 migration이 성공한다.
