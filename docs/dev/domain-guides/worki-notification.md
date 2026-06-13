# Worki Notification

## 개요

사용자 알림창은 알림 이력을 기준으로 전체, 티켓, 게시판, 매뉴얼 탭을 조회한다.
이 문서는 티켓, 게시판, 매뉴얼 탭에서 생성되어야 하는 시스템 알림 타입과 도메인 상태 매핑을 정리한다.

## 티켓 탭 알림

티켓 탭은 사용자가 발행한 티켓의 상태 변화에 따라 생성된 알림을 보여준다.

| 상황 | 도메인 상태 | 알림 타입 | 알림 메시지 |
| --- | --- | --- | --- |
| 티켓에 담당 부서가 배정된 경우 | `tickets.status = ASSIGNED` | `notifications.type = TICKET_ASSIGNED` | 티켓 부서 배정 |
| 티켓에 답변이 생성된 경우 | `tickets.status = COMPLETED` | `notifications.type = TICKET_COMPLETED` | 티켓 답변 완료 |
| 티켓이 삭제된 경우 | `tickets.status = DELETED` | `notifications.type = TICKET_DELETED` | 티켓 삭제 |

### 생성 기준

- 티켓 상태가 `ASSIGNED`, `COMPLETED`, `DELETED`로 변경되는 시점에 알림을 생성한다.
- 알림 수신자는 해당 티켓을 발행한 사용자다.
- 티켓 탭 조회는 현재 티켓 상태가 아니라 `notifications`에 저장된 알림 이력을 기준으로 한다.

## 게시판 탭 알림

게시판 탭은 워키 질문/답변 활동에 따라 생성된 알림을 보여준다.

| 상황 | 도메인 상태 | 알림 타입 | 알림 메시지 |
| --- | --- | --- | --- |
| 내가 작성한 질문에 답변이 생성된 경우 | `worki_questions.status = IN_PROGRESS` | `notifications.type = WORKI_QUESTION_ANSWERED` | 워키 답변 완료 |
| 내가 작성한 답변이 채택된 경우 | `worki_questions.status = ANSWERED` | `notifications.type = WORKI_ANSWER_ACCEPTED` | 워키 답변 채택 |

### 생성 기준

- 질문 등록 성공 안내는 알림함에 저장하지 않고 프론트 토스트 등 즉시 피드백으로 처리한다.
- `WORKI_QUESTION_CREATED` 타입은 기존 데이터 호환을 위해 유지하지만, 신규 질문 등록 시에는 생성하지 않는다.
- 내 질문에 다른 사용자가 답변을 등록하면 `WORKI_QUESTION_ANSWERED` 알림을 생성한다.
- 내가 작성한 답변이 채택되면 `WORKI_ANSWER_ACCEPTED` 알림을 생성한다.
- 게시판 탭 조회는 현재 질문 상태가 아니라 `notifications`에 저장된 알림 이력을 기준으로 한다.

## 매뉴얼 탭 알림

매뉴얼 탭은 기존 매뉴얼이 수정되어 새 버전 이력이 생성된 경우의 알림을 보여준다.

| 상황 | 도메인 기준 | 알림 타입 | 알림 메시지 |
| --- | --- | --- | --- |
| 기존 매뉴얼이 수정된 경우 | `manual_versions` row 생성 및 `manuals.status = PUBLISHED` | `notifications.type = MANUAL_UPDATED` | 매뉴얼이 업데이트되었습니다 |

### 생성 기준

- `AdminManualService.update()`와 `AdminManualService.updateFromPdf()`에서만 알림을 생성한다.
- 매뉴얼 신규 생성, PDF 신규 생성, 삭제 시에는 알림을 생성하지 않는다.
- 수정 후 매뉴얼 상태가 `PUBLISHED`인 경우에만 알림을 생성한다.
- 알림 수신자는 삭제되지 않은 전체 활성 사용자다.
- 알림 생성은 `notification_settings`와 무관하며, 알림창 이력 생성을 위해 항상 `notifications`에 저장한다.
- 알림 메시지에는 `manual_versions.manual_num` 버전 정보와 `manual_versions.update_reason` 수정 요약을 포함한다.
- 알림 클릭 시 `target_url = /manuals/{manualId}` 경로로 이동한다.
- 매뉴얼 탭 조회는 현재 매뉴얼 상태가 `PUBLISHED`인 `MANUAL_UPDATED` 알림 이력을 기준으로 한다.

## 조회 기준

- 전체 탭은 삭제되지 않은 모든 알림을 최신순으로 조회한다.
- 티켓 탭은 `target_type = TICKET`이고 타입이 티켓 알림 타입인 알림만 조회한다.
- 게시판 탭은 `target_type`이 `WORKI_QUESTION` 또는 `WORKI_ANSWER`이고 타입이 게시판 알림 타입인 알림만 조회한다.
- 매뉴얼 탭은 `target_type = MANUAL`이고 타입이 `MANUAL_UPDATED`인 알림만 조회한다.
- 삭제된 알림은 `deleted_at`이 존재하므로 알림창 목록에서 제외한다.

## 관리자 수기 지식 알림

관리자 수기 지식 알림은 별도 탭을 만들지 않고 기존 매뉴얼 탭에 함께 노출한다.

| 상황 | 도메인 기준 | 알림 타입 | 이동 경로 |
| --- | --- | --- | --- |
| 수기 지식이 활성 상태로 등록된 경우 | `direct_data.is_active = 'Y'` | `notifications.type = DIRECT_DATA_ACTIVATED` | `/direct-data/{directDataId}` |
| 비활성 수기 지식이 활성화된 경우 | `direct_data.is_active = 'N'` -> `'Y'` | `notifications.type = DIRECT_DATA_ACTIVATED` | `/direct-data/{directDataId}` |

### 생성 기준

- 관리자 수기 지식 `create()`에서 저장 결과가 활성 상태이면 알림을 생성한다.
- 관리자 수기 지식 `update()`에서 기존 비활성 데이터가 활성 상태로 바뀌면 알림을 생성한다.
- 비활성 상태로 등록되거나, 이미 활성 상태인 데이터를 수정하는 경우에는 중복 알림을 생성하지 않는다.
- 알림 수신자는 삭제되지 않은 전체 활성 사용자다.
- 알림 생성은 `notification_settings`와 무관하게 알림창 이력 생성을 위해 `notifications`에 저장한다.
- 매뉴얼 탭 조회는 `MANUAL_UPDATED` 알림과 `DIRECT_DATA_ACTIVATED` 알림을 함께 반환한다.
- 수기 지식 알림은 현재 활성 상태이고 삭제되지 않은 `direct_data`만 매뉴얼 탭에 노출한다.
