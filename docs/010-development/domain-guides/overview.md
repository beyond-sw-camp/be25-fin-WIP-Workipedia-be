# Domain Development Guide — Workipedia

> 문서 유형: Development Guide
> 상태: Draft
> 정본 위치: `docs/010-development/domain-guides/overview.md`
> 관련 문서: `docs/001-reference/service-flow.md`, `docs/004-api/api-contract.md`, `docs/005-database/db-migration-guide.md`, `docs/006-planning/wbs.md`
> 버전: v0.1
> 최종 수정: 2026-05-31

백엔드 개발을 시작할 때 도메인별로 어떤 문서를 먼저 보고, 어떤 범위부터 구현할지 정리한다.
개발자는 기능 구현 전에 본 문서와 담당 도메인 가이드를 확인한다.

## 개발 전 공통 확인 순서

1. `docs/006-planning/daily-plans/YYYY-MM-DD.md`
2. `docs/006-planning/weekly-wbs/`
3. 본인 `docs/006-planning/member-wbs/{name}.md`
4. 관련 ADR
5. `docs/004-api/api-contract.md`
6. `docs/005-database/db-migration-guide.md`
7. 담당 도메인 가이드

## 도메인별 가이드

| 도메인 | 담당 | 가이드 | 핵심 참고 문서 |
|---|---|---|---|
| Auth | 이슬이 | `auth.md` | ADR 003, API Contract, PRD |
| Ticket | 김진혁 | `ticket.md` | ADR 004, ADR 005, Service Flow |
| Chatbot/RAG | 김진혁, 이슬이 | `chatbot-rag.md` | ADR 002, ADR 008, Harness Guide |
| Worki/FAQ/Notification | 민정기 | `worki-notification.md` | ADR 006, ADR 007, Service Flow |
| Admin/Point/Badge/ESG | 김가영 | `admin-reward-esg.md` | ADR 005, ADR 006, Project Proposal |
| Frontend Integration | 황희수 | `frontend-integration.md` | API Contract, Service Flow, Figma 기준 |

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
