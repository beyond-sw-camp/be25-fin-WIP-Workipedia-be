# Daily Report — 김진혁 2026-06-01

> 문서 유형: Daily Report
> 상태: Draft
> 정본 위치: `docs/006-planning/daily-reports/2026-06-01/kim-jinhyeok.md`
> 관련 문서: `docs/006-planning/daily-plans/2026-06-01.md`, `docs/006-planning/weekly-wbs/2026-06-01-week1.md`, `docs/006-planning/member-wbs/kim-jinhyeok.md`
> 버전: v0.1
> 최종 수정: 2026-06-01

## 완료

- 티켓 생성/목록/상세 조회 API skeleton 구현
- 티켓 상태값 `RECEIVED`, `COMMON_QUEUE`, `ASSIGNED`, `IN_PROGRESS`, `COMPLETED` 코드 반영
- 티켓 담당자 배정 API skeleton 구현
- 티켓 라우팅을 `TicketRoutingAiClient` 경계로 분리하고 실제 AI 구현체 연결 전에는 공통 접수 큐 fallback만 수행
- `code/status/message/data` 공통 응답 wrapper 추가
- 인메모리 티켓 repository 제거 및 JPA `TicketRepository` 전환
- `Ticket` 엔티티 skeleton 구성, 로컬/테스트 모두 `.env` 기반 MariaDB profile로 분리
- Docker Compose 기반 MariaDB local/test DB 구성 추가
- Flyway를 기본 활성화. 단, `V1` 초기 전체 스키마는 팀 합의 후 `V1__create_initial_schema.sql`로 작성 필요
- 티켓 API MockMvc 테스트 작성 및 `./gradlew test` 통과
- 실제 개발량 기준으로 Week 1 WBS와 김진혁 개인 WBS를 기능/API 구현 중심으로 상향 조정

## 미완료

- `ticket_routing_logs`, `ticket_assignments` 실제 저장 구조 반영
- 챗봇 실패 응답의 `CREATE_TICKET` 액션과 티켓 초안 연결 구현
- local LLM 또는 분류 모델 기반 실제 `TicketRoutingAiClient` 구현
- TEAM_ADMIN 이관 요청 및 공통 접수 큐 재배정 API 구현
- local RAG seed 검색 skeleton 구현

## 다음 근무일 논의

- `RECEIVED` 상태를 DB에 실제로 남길지, 생성 직후 라우팅 후 상태만 저장할지 결정 필요
- 라우팅 신뢰도 초기 기준을 80 이상 자동 배정, 50 이상 관리자 검토, 50 미만 공통 접수 큐로 고정할지 확인
- 챗봇 실패 질문을 티켓 초안으로 넘길 때 필요한 필드 확정 필요
- 인증/권한 적용 전 skeleton API를 permit-all로 둘 수 있는 기간 확인 필요
- 상향 조정된 Week 1 WBS를 팀원별 기능 Issue로 쪼개고 담당자별 동의 필요
- `harness-engineering.md`에 Kafka가 명시되어 있으나 proposal/TRD/ADR 어디에도 도입 결정 없음. Kafka 사용 여부 팀 결정 후 도입 시 ADR 작성 필요, 미도입 시 harness에서 제거 필요
- 챗봇 API 구조 논의 필요: Notion 명세는 flat(`/chatbot/messages`), 현재 contract는 세션 기반(`/chatbot/sessions/{id}/messages`). DB 스키마가 세션 기반(`chatbot_sessions` 테이블)으로 설계되어 있어 세션 기반으로 가닥을 잡았으나 이슬이와 최종 합의 필요

## API/DB/화면 영향

- API: `POST /api/v1/tickets`, `GET /api/v1/tickets`, `GET /api/v1/tickets/{ticketId}`, `PATCH /api/v1/tickets/{ticketId}/status`, `PATCH /api/v1/tickets/{ticketId}/assignee` skeleton 추가
- DB: `tickets`는 JPA 엔티티 skeleton으로 전환, local/test profile 모두 MariaDB `.env` 기반. Docker Compose로 `workipedia_local`, `workipedia_test` 생성 가능. Flyway는 기본 활성화했지만 초기 전체 스키마 migration은 아직 작성하지 않음
- 화면: 요청 티켓 생성, 티켓 목록/상세, 담당자 배정 화면에서 mock 연동 가능
- 일정: Week 1 목표가 skeleton 중심에서 핵심 API 1차 구현 중심으로 변경됨

## 관련 링크

- `src/main/java/com/wip/workipedia/ticket/`
- `src/test/java/com/wip/workipedia/ticket/controller/TicketControllerTest.java`
