# Ticket Domain Guide

> 문서 유형: Development Guide
> 상태: Draft
> 정본 위치: `docs/dev/domain-guides/ticket.md`
> 관련 문서: `docs/adr/004-ticket-routing-strategy.md`, `docs/adr/005-role-permission-strategy.md`, `docs/reference/service-flow.md`, `docs/api/api-contract.md`
> 버전: v0.1
> 최종 수정: 2026-06-09

## 개발 목표

요청을 티켓으로 발행하고, 자동 배정 또는 공통 접수 큐를 거쳐 담당 부서와 팀원에게 연결한다.

## 먼저 볼 문서

- `docs/adr/004-ticket-routing-strategy.md`
- `docs/adr/005-role-permission-strategy.md`
- `docs/reference/service-flow.md`
- `docs/api/api-contract.md`
- `docs/dev/db-migration-guide.md`

## MVP 구현 범위

- 요청 티켓 생성
- 라우팅 점수 저장
- 담당 부서 자동 배정
- 낮은 점수일 때 공통 접수 큐 이동
- `TEAM_ADMIN`의 팀원 배정
- `TEAM_ADMIN`의 이관 요청 시 공통 접수 큐 이동
- 티켓 상태 변경
- 본인 티켓 조회
- 상태별/부서별 티켓 목록 조회
- 팀 티켓 큐 조회
- 공통 접수 큐 조회
- 티켓 중요도(priority) 저장
- 부서 R&R과 승인 사례 기반 후보 부서 Top 3 검색 및 Cross-Encoder 재정렬
- 사진 첨부 업로드/조회

## API/DB 영향

- `tickets`
- `ticket_status`
- `priority`
- `assigned_department_id`
- `assignee_id`
- `ticket_transfer_requests`
- `routing_confidence_score`
- reranker `top_score`, `score_margin`, 후보별 `score`, `rank`
- `transfer_reason`, `suggested_department_id`
- `attachments`
- ticket create/list/detail/update APIs
- `GET /tickets?status={status}&departmentId={departmentId}`
- `POST /team/tickets/{ticketId}/transfer`
- `GET /admin/common-queue/tickets`
- `PATCH /admin/common-queue/tickets/{ticketId}/department`
- attachment upload/read APIs
- `StoragePort` 기반 R2/S3/MinIO Object Storage
- presigned upload/download API

## 티켓 이관 정책

- AI 라우팅으로 담당 부서가 배정된 티켓은 `tickets.status=ASSIGNED` 상태가 된다.
- 담당 부서의 `TEAM_ADMIN`만 자기 부서에 배정된 `ASSIGNED` 티켓에 대해 이관을 요청할 수 있다.
- 이관 요청 시 티켓은 다른 부서로 직접 이동하지 않고 공통 접수 큐의 이관 대기 항목으로 이동한다.
- 이관 요청 시 `tickets.status=TRANSFERRED`, `ticket_transfer_requests.status=REQUESTED`로 저장한다.
- 이관 요청에는 이관 사유가 필수이며, 후보 부서는 선택값이다.
- 공통 접수 큐 관리자인 `SYSTEM_ADMIN`은 `TRANSFERRED` 티켓의 이관 사유와 후보 부서를 확인한다.
- `SYSTEM_ADMIN`이 적절한 담당 부서로 재배정하면 `tickets.status=ASSIGNED`, `ticket_transfer_requests.status=ASSIGNED_FROM_QUEUE`로 변경한다.
- 재배정된 티켓은 새 담당 부서의 팀 큐에서 `ASSIGNED` 티켓으로 조회된다.
- 일반 공통 접수 큐 배정은 `TICKET_ASSIGNED` 알림을 생성하고, 이관 티켓 재배정은 `TICKET_REASSIGNED` 알림을 생성한다.

## 권한/보안 체크

- `USER`는 본인 티켓만 조회한다.
- `TEAM_ADMIN`은 자기 팀 티켓만 조회한다.
- `SYSTEM_ADMIN`은 공통 접수 큐를 관리한다.
- 팀 관리자의 이관은 다른 부서 직접 이동이 아니라 공통 접수 큐 이동이다.
- AI는 부서까지만 추천하며 개인 담당자는 `TEAM_ADMIN`이 배정한다.

## 완료 기준

- 사용자가 요청 티켓을 생성할 수 있다.
- 라우팅 점수에 따라 담당 부서 또는 공통 접수 큐로 이동한다.
- 팀 관리자가 팀원에게 티켓을 배정할 수 있다.
- 팀 관리자가 자기 부서의 배정 티켓에 대해 이관을 요청할 수 있다.
- 이관 요청된 티켓은 공통 접수 큐에서 이관 사유와 후보 부서를 확인할 수 있다.
- 시스템 관리자가 이관 요청 티켓을 담당 부서로 재배정할 수 있다.
- 담당 팀원이 처리 완료 상태로 변경할 수 있다.
- `status`, `departmentId` 조건으로 티켓 목록을 조회할 수 있다.
- 티켓 생성 시 중요도와 첨부 파일이 저장된다.
- 첨부 바이너리는 선택된 Object Storage에 저장되고 DB에는 object key와 파일 메타데이터만 저장된다.
- provider 변경 시 티켓 도메인 코드는 수정하지 않는다.
- 라우팅 기준을 통과하지 못하면 후보 부서와 점수를 남기고 공통 접수 큐로 이동한다.

## 논의 필요 사항

- `RECEIVED` 상태를 DB에 실제로 남길지
- Cross-Encoder 1위 최소 점수와 1·2위 최소 점수 차이
- 담당자 변경 허용 여부
- 반려/취소 상태를 MVP에 넣을지 여부
- 업로드 완료 후 첨부 메타데이터 등록 실패 시 orphan object 정리 방식
