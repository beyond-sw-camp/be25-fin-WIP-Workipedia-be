# Workipedia

AI 기반 사내 지식 공유 플랫폼 (한화 계열사 대상). 5인 팀 프로젝트.

## Tech Stack

- Java 21, Spring Boot 3.x
- MariaDB (Spring Data JPA + Flyway), Redis
- Spring Security (현재 permit-all skeleton, Auth 미구현)
- Spring AI (ticket AI 라우팅 adapter)
- Kafka, Quartz, Spring Mail

## Package Structure

```
com.wip.workipedia
├── ticket/        # 티켓 CRUD + AI 라우팅 (구현됨)
│   ├── controller/
│   ├── domain/    # Ticket, TicketStatus, RoutingDecision
│   ├── dto/
│   ├── repository/
│   ├── service/   # TicketService, TicketRoutingService
│   └── ai/        # TicketRoutingAiClient(interface), FallbackTicketRoutingAiClient
├── auth/          # skeleton only (handler 2개)
├── common/        # ApiResponse, PageResponse, CustomException, ErrorType, GlobalExceptionHandler
└── config/        # SecurityConfig (permit-all)
```

## Key Docs

| 목적 | 경로 |
|---|---|
| API 계약 | `docs/api/api-contract.md` |
| 도메인별 개발 가이드 | `docs/dev/domain-guides/` |
| ADR (기술 결정) | `docs/adr/` |
| DB 마이그레이션 가이드 | `docs/dev/db-migration-guide.md` |
| Git 브랜치 전략 | `docs/dev/git-strategy.md` |
| 작업 완료 기준 (DoD) | `docs/dev/definition-of-done.md` |
| 개인 WBS | `docs/planning/member-wbs/{name}.md` |
| PRD / TRD | `docs/reference/prd.md`, `docs/reference/trd.md` |

## DB Migration

- Flyway 사용, `src/main/resources/db/migration/`
- `V1__create_initial_schema.sql` — 전체 스키마 (테이블 30개+)
- 테스트 환경: `validate-on-migrate=false`

## 현재 개발 상태 (2026-06-02 기준)

- **Ticket CRUD**: 완성, `dev` 브랜치에 머지됨 (PR #29)
- **Auth**: 미구현 — `TicketService.SKELETON_REQUESTER_ID = 1L` 하드코딩 상태
- **AI 라우팅**: `TicketRoutingAiClient` interface만 있고 실제 구현 없음 (FallbackTicketRoutingAiClient만)
- **RAG**: DB에 `manual_chunks.embedding_json`, `worki_chunks.embedding_json` 컬럼 준비됨

## 브랜치 전략

```
feat/*, docs/*, chore/* → dev → main
```

작업 시작 전 `dev`에서 브랜치 따기.

## 팀원 & 담당 도메인

| 이름 | 도메인 |
|---|---|
| 김진혁 | 티켓, RAG, 문서 |
| 황희수 | 프론트엔드 |
| 김가영 | Auth, 회원 |
| 이슬이 | Worki Q&A |
| 민정기 | Elasticsearch, Admin |
