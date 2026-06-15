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

## Git Rules

- 구현 중 자동으로 커밋하지 않는다. 변경 사항을 staged/unstaged 상태로 두고 사용자가 검토한 뒤 커밋한다.
- 커밋 메시지는 한국어로 작성한다. 예: `feat: 챗봇 세션 메시지 저장 구현`
- 커밋 메시지에 `Co-Authored-By:` 태그를 붙이지 않는다.
- 구현 시작 전 `dev`에서 새 브랜치를 만든다. `dev`나 `main`에 직접 작업하지 않는다.
- 사용자가 커밋이나 PR 생성을 명시적으로 요청한 경우에만 해당 작업을 수행한다.

## PR Rules

- PR을 작성하기 전에 대상 레포가 `Workipedia-be`인지 확인하고 `.github/PULL_REQUEST_TEMPLATE`을 다시 읽는다.
- PR 제목과 본문은 해당 템플릿의 순서와 제목을 유지한다.
- 사용자에게 PR 본문을 제공할 때는 복사할 수 있도록 전체 내용을 `markdown` 코드 블록으로 감싼다.
- 관련 이슈는 `close #123`, `fixes #123`, `resolves #123` 중 하나로 연결한다.

PR 본문 형식:

```markdown
## 📌 PR 제목
## ✨ 작업 내용
## 🔧 변경 사항
## 📸 실행 결과 (선택)
## ⚠️ 참고 사항
## 📎 관련 이슈
```

## Issue Rules

- 이슈를 작성하기 전에 대상 레포가 `Workipedia-be`인지 확인하고 `.github/ISSUE_TEMPLATE/`의 실제 템플릿을 다시 읽는다.
- Feature, Bug, Refactor 중 작업 성격에 맞는 템플릿을 사용하고 제목 prefix와 label을 맞춘다.
- 사용자에게 이슈 본문을 제공할 때는 복사할 수 있도록 전체 내용을 `markdown` 코드 블록으로 감싼다.
- 템플릿 항목을 임의로 삭제하거나 순서를 바꾸지 않는다. 해당하지 않는 항목은 `해당 없음`으로 명시한다.

Feature (`[FEAT]`, `feature`):

```markdown
## 🧩 기능 설명
## 📂 관련 도메인
## 🔐 권한
## ⚠️ 고려사항
## ⚠️ 예외 케이스
## ✅ 완료 기준
```

Bug (`[BUG]`, `bug`):

```markdown
## 🐛 문제 요약
## 📍 발생 위치
## 🔁 재현 방법
## 🎯 기대 결과
## ❗ 실제 결과
## 💥 영향 범위
## 🧠 추정 원인 (선택)
## 🛠 수정 방향
## ✅ 완료 조건
```

Refactor (`[REFACTOR]`, `refactor`):

```markdown
## ♻️ 리팩토링 대상
## 🚨 현재 문제
## 🛠 개선 방향
## 🔍 영향 도메인
## ⚠️ 위험 요소
## ✅ 완료 조건
```

## Development Rules

- Controller, Service, Repository, Domain의 기존 패키지 구조와 구현 패턴을 우선한다.
- DB 변경은 `V1__create_initial_schema.sql`을 수정하지 않고 새 Flyway migration으로 추가한다.
- 코드 변경 시 관련 API 계약과 domain guide를 함께 갱신한다.
- 민감정보, 비밀번호, API key, DB 접속정보를 코드나 로그에 기록하지 않는다.
- `.env`와 운영 credential은 커밋하지 않는다.
