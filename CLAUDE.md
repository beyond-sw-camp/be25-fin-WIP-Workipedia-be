# Workipedia

AI 기반 사내 지식 공유 플랫폼 (한화 계열사 대상). 5인 팀 프로젝트.

## Tech Stack

- Java 21, Spring Boot 3.x
- MariaDB (Spring Data JPA + Flyway), Redis
- Spring Security (현재 permit-all skeleton, Auth 미구현)
- Spring AI (ticket AI 라우팅 adapter)
- RabbitMQ, Spring Scheduler (`@Scheduled`), Spring Mail

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

## 개발 중 문서 업데이트 규칙

각 팀원은 본인 담당 도메인 개발 시 아래 문서를 직접 업데이트한다.

| 문서 | 경로 | 업데이트 내용 |
|---|---|---|
| PRD 요구사항 명세서 | `docs/reference/prd.md` | 본인 담당 섹션 요구사항 구체화 |
| API 명세서 | `docs/api/api-contract.md` | 구현한 API endpoint 추가/수정 |
| Domain Guide | `docs/dev/domain-guides/{domain}.md` | 구현 방식, 경계, 주의사항 업데이트 |
| DB 마이그레이션 | `src/main/resources/db/migration/` | V1 수정 금지 — 변경 필요 시 V2, V3 신규 파일로 추가 |

## 브랜치 전략

```
feat/*, docs/*, chore/* → dev → main
```

작업 시작 전 `dev`에서 브랜치 따기.

## 팀원 & 담당 도메인

| 이름 | 도메인 |
|---|---|
| 김진혁 | 티켓, AI/RAG 연동, 챗봇 답변·세션·메시지 저장, 지식화, CI/CD |
| 이슬이 | Auth, 사용자 관리, 보안, 알림, 포인트, ESG 등급·지표 |
| 민정기 | 워키 게시판, FAQ, Elasticsearch, 매뉴얼, 프론트엔드 |
| 김가영 | 관리자 기능, 부서 관리, 관리자 대시보드 |
| 황희수 | 프론트엔드 공동 담당 |

관리자 기능의 세부 도메인은 위 담당을 우선한다. 사용자 관리는 이슬이, 매뉴얼은 민정기, 부서 관리는 김가영이 담당한다.
