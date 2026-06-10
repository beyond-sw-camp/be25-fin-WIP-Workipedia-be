# Development Guide — Workipedia

> 문서 유형: Development Guide
> 상태: Draft
> 정본 위치: `docs/dev/development-guide.md`
> 관련 문서: `docs/reference/service-flow.md`, `docs/api/api-contract.md`, `docs/dev/db-migration-guide.md`, `docs/planning/wbs.md`
> 버전: v0.1
> 최종 수정: 2026-06-04

개발을 시작할 때 어떤 문서를 먼저 보고, 개발 후 어떤 문서를 갱신해야 하는지 정리한다.
개발자는 기능 구현 전에 본 문서와 담당 도메인 가이드를 확인한다.

## 개발 전 공통 확인 순서

1. `docs/planning/wbs.md`
2. 본인 `docs/planning/member-wbs/{name}.md`
3. 담당 도메인 가이드: `docs/dev/domain-guides/{domain}.md`
4. 관련 ADR: `docs/adr/*.md`
5. `docs/reference/prd.md`
6. `docs/reference/service-flow.md`
7. `docs/api/api-contract.md`
8. DB 변경이 있으면 `docs/dev/db-migration-guide.md`와 `src/main/resources/db/migration/*.sql`

## 개발 후 문서 갱신 규칙

기능을 구현하거나 정책을 바꾼 팀원은 PR 전에 아래 문서를 함께 확인한다.

| 변경 내용 | 반드시 갱신할 문서 |
|---|---|
| API path/request/response/status/enum 변경 | `docs/api/api-contract.md` |
| 기능 요구사항, 우선순위, 사용자 시나리오 변경 | `docs/reference/prd.md` |
| 본인 도메인의 구현 범위, 완료 기준, 논의 사항 변경 | `docs/dev/domain-guides/{domain}.md` |
| 질문/요청/Flash Chat 등 사용자 흐름 변경 | `docs/reference/service-flow.md` |
| DB 테이블/컬럼/제약 변경 | `src/main/resources/db/migration/*.sql`, `docs/reference/trd.md`, `docs/dev/db-migration-guide.md` |

문서 갱신은 별도 문서 작업이 아니라 기능 개발 완료 기준의 일부로 본다.

## 도메인별 가이드

| 도메인 | 담당 | 가이드 | 핵심 참고 문서 |
|---|---|---|---|
| Auth | 이슬이 | `auth.md` | `docs/adr/003-auth-strategy.md`, API Contract, PRD |
| Ticket | 김진혁 | `ticket.md` | `docs/adr/004-ticket-routing-strategy.md`, `docs/adr/005-role-permission-strategy.md`, Service Flow |
| Chatbot/RAG | 김진혁, 이슬이 | `chatbot-rag.md` | `docs/adr/002-rag-strategy.md`, `docs/adr/008-local-llm-security-strategy.md`, Harness Guide |
| Worki/FAQ | 민정기 | `worki-notification.md` | `docs/adr/006-knowledge-conversion-strategy.md`, Service Flow |
| Notification | 이슬이 | `worki-notification.md` | `docs/adr/007-notification-strategy.md`, Service Flow |
| Admin/Point/ESG Grade/ESG | 김가영 | `admin-reward-esg.md` | `docs/adr/005-role-permission-strategy.md`, `docs/adr/006-knowledge-conversion-strategy.md`, PRD |
| Frontend Integration | 황희수, 민정기 | `frontend-integration.md` | API Contract, Service Flow, Figma 기준 |

## 개발 시작 기준

각 도메인은 바로 기능 전체를 구현하지 않고 아래 순서로 시작한다.

```text
Issue 생성
-> 브랜치 생성
-> Entity/DTO/Controller/Service/Repository skeleton
-> API request/response 확인
-> DB migration 확인
-> 최소 happy path 구현
-> 권한/실패 케이스 추가
-> 테스트 또는 harness 기준 확인
-> PR
```

## 도메인 가이드 작성 규칙

각 도메인 가이드는 다음 항목을 가진다.

- 개발 목표
- 먼저 볼 문서
- MVP 구현 범위
- API/DB 영향
- 권한/보안 체크
- 완료 기준
- 논의 필요 사항
