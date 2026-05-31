# Reference Document Guide

> 문서 유형: Reference Guide
> 상태: Draft
> 정본 위치: `docs/001-reference/document-guide.md`
> 관련 문서: `docs/001-reference/constitution.md`
> 버전: v0.2
> 최종 수정: 2026-05-31

팀원이 프로젝트의 제품/기술/원칙 문서를 같은 기준으로 참고하도록 정리한다.
루트 `README.md`는 최종 산출물 정리용으로 유지하고, 작업 중 참고 문서는 `docs/` 아래에서 관리한다.

## 문서 목록

| 문서 | 파일 | 역할 |
|---|---|---|
| Constitution | `constitution.md` | 프로젝트 불변 원칙과 의사결정 기준 |
| Project Proposal | `project-proposal.md` | 문제 정의, 서비스 구조, MVP 범위, 일정이 포함된 기획서 |
| Service Flow | `service-flow.md` | 질문/요청 분리, 티켓 라우팅, 지식화 시나리오 |
| PRD | `prd.md` | 제품 요구사항, 사용자, 기능 범위, 성공 지표 |
| TRD | `trd.md` | 기술 구조, 데이터 모델, API/보안/운영 요구사항 |
| Project Structure | `../002-architecture/project-structure.md` | 모듈러 모놀리스 구조와 도메인 경계 |
| ADRs | `../003-adr/` | DB/Auth/RAG 등 주요 기술 결정 기록 |
| API Contract | `../004-api/api-contract.md` | 프론트/백엔드 요청·응답 계약 |
| DB Migration Guide | `../005-database/db-migration-guide.md` | Flyway migration 규칙과 테이블 생성 순서 |
| WBS | `../006-planning/wbs.md` | 팀 역할, 일정, 주차별 작업 |
| Today | `../006-planning/today.md` | 팀원이 "나 오늘 뭐하면 돼?"라고 물었을 때 보는 당일 작업 진입점 |
| Daily Work Plan | `../006-planning/daily-work-plan.md` | 담당자별 하루 단위 실행 계획 |
| Weekly WBS | `../006-planning/weekly-wbs/` | 금요일마다 확정하는 다음 주 실행 WBS |
| Daily Report Guide | `../006-planning/daily-reports/guide.md` | 개인별 하루 작업 보고 작성 규칙 |
| Daily Reports | `../006-planning/daily-reports/` | 근무일별 개인 작업 보고 |
| Daily Discussion Guide | `../006-planning/daily-discussions/discussion-guide.md` | daily report 기반 팀 논의사항 작성 규칙 |
| Daily Discussions | `../006-planning/daily-discussions/` | 필요 시 작성하는 팀 논의/결정사항 |
| Member WBS | `../006-planning/member-wbs/` | 개인별 작업 범위 |
| Definition of Done | `../006-planning/definition-of-done.md` | 작업 완료 판단 기준 |
| Harness Guide | `../007-quality/harness-engineering.md` | 출처, 개인정보, 권한, 워크플로우 검증 기준 |
| Midterm Guide | `../008-presentation/midterm-presentation-guide.md` | 중간 발표 메시지와 시연 구성 |
| Git Strategy | `../009-process/git-strategy.md` | 브랜치, 커밋, PR, 리뷰 규칙 |
| Agent Workflow | `../010-development/agent-workflow.md` | Codex/Claude 등 에이전트 기반 작업 절차 |

## 디렉터리 우선순위

| 순서 | 디렉터리 | 기준 |
|---:|---|---|
| 001 | `001-reference` | 헌법, 서비스 흐름, PRD/TRD처럼 모든 문서의 기준이 되는 문서 |
| 002 | `002-architecture` | 구현 구조와 도메인 경계를 잡는 문서 |
| 003 | `003-adr` | 주요 기술 선택과 의사결정 기록 |
| 004 | `004-api` | 프론트/백엔드 연동 계약 |
| 005 | `005-database` | DB migration과 상태값 기준 |
| 006 | `006-planning` | WBS, 주간 실행 계획, 데일리 논의사항, 개인별 작업 범위 |
| 007 | `007-quality` | 하네스, 테스트, 품질 기준 |
| 008 | `008-presentation` | 발표 메시지와 시연 흐름 |
| 009 | `009-process` | Git, PR, 리뷰 등 협업 프로세스 |
| 010 | `010-development` | 로컬 개발환경, 실행 방법 등 개발 편의 문서 |

## 읽는 순서

1. `constitution.md`
2. `project-proposal.md`
3. `service-flow.md`
4. `prd.md`
5. `trd.md`
6. `../002-architecture/project-structure.md`
7. `../003-adr/0001-database-choice.md`
8. `../003-adr/0002-rag-strategy.md`
9. `../003-adr/0003-auth-strategy.md`
10. `../004-api/api-contract.md`
11. `../005-database/db-migration-guide.md`
12. `../006-planning/wbs.md`
13. `../006-planning/today.md`
14. `../006-planning/daily-work-plan.md`
15. `../006-planning/weekly-wbs/`
16. `../006-planning/daily-reports/guide.md`
17. `../006-planning/daily-discussions/`
18. `../006-planning/definition-of-done.md`
19. `../007-quality/harness-engineering.md`
20. `../008-presentation/midterm-presentation-guide.md`
21. `../009-process/git-strategy.md`
22. `../010-development/agent-workflow.md`

## 관리 규칙

- 정책이나 원칙 변경은 `constitution.md`와 충돌하지 않아야 한다.
- 질문/요청/티켓/지식화 흐름 변경은 `service-flow.md`에 먼저 반영한다.
- 제품 범위 변경은 `prd.md`에 먼저 반영한 뒤 구현한다.
- 기술 구조, 테이블, API 변경은 `trd.md` 또는 별도 기술 문서에 반영한다.
- 당일 작업 시작점은 `../006-planning/today.md`로 둔다.
- skeleton 또는 기능 개발 시작 전에는 GitHub Issue를 먼저 만든다.
- 매주 금요일에는 다음 주 실행 WBS를 `../006-planning/weekly-wbs/`에 작성하고, 다음 주 Weekly Issue 내용을 준비한다.
- 매일 작업 종료 전에는 본인 daily report를 `../006-planning/daily-reports/YYYY-MM-DD/{member}.md`에 작성한다.
- 팀 합의가 필요한 논의사항은 필요할 때만 `../006-planning/daily-discussions/`에 작성한다.
- 루트 `README.md`는 발표/최종 산출물 정리 시점에만 갱신한다.
- 작업 중 문서는 `docs/` 아래에 만들고, 루트 `README.md`에 임의 링크를 추가하지 않는다.

## 문서 수정 권한 기준

문서는 영향 범위에 따라 자유 수정, 담당자 리뷰, 팀 합의 문서로 나눈다.
문서 수정 권한은 코드 소유권이 아니라 충돌과 혼선을 줄이기 위한 운영 기준이다.

### 자유 수정 가능

본인 작업 진행상황과 개인 기록에 해당하는 문서는 자유롭게 수정할 수 있다.

| 문서 | 기준 |
|---|---|
| `../006-planning/daily-reports/YYYY-MM-DD/{member}.md` | 본인 daily report |
| `../006-planning/member-wbs/{본인}.md` | 본인 작업 범위와 체크리스트 |

규칙:

- 본인 파일만 수정한다.
- 다른 사람 daily report나 member WBS는 임의 수정하지 않는다.
- PR은 `docs: YYYY-MM-DD 이름 daily report 작성` 형식으로 올린다.

### 담당자 리뷰 후 수정

API, DB, 일정, 화면, 협업 규칙처럼 다른 담당자 작업에 영향을 주는 문서는 관련 담당자 리뷰를 받고 수정한다.

| 문서 | 리뷰 기준 |
|---|---|
| `../004-api/api-contract.md` | 프론트 담당 + 해당 백엔드 담당 |
| `../005-database/db-migration-guide.md` | 백엔드 담당, DB 영향이 큰 경우 2명 |
| `../006-planning/weekly-wbs/` | 김진혁 또는 일정 담당 |
| `../006-planning/today.md` | 김진혁 |
| `../002-architecture/project-structure.md` | 영향 도메인 담당 |
| `../009-process/git-strategy.md` | 김진혁 또는 팀 합의 |

규칙:

- API request/response, 상태값, 권한, DB 컬럼 변경은 담당자 확인 후 반영한다.
- 프론트 화면에 영향이 있으면 황희수에게 공유한다.
- Git/PR 운영 방식 변경은 팀 전체가 따라야 하므로 PR 설명에 변경 이유를 적는다.

### 팀 합의 후 수정

서비스 방향, 핵심 정책, 역할/권한, 발표 메시지에 영향을 주는 문서는 팀 합의 후 수정한다.

| 문서 | 기준 |
|---|---|
| `constitution.md` | 프로젝트 원칙과 의사결정 기준 |
| `service-flow.md` | 질문/요청/티켓/지식화 핵심 흐름 |
| `prd.md` | 제품 범위, 기능 우선순위, 성공 지표 |
| `trd.md` | 기술 구조, 주요 데이터 모델, 보안/운영 요구사항 |
| `project-proposal.md` | 기획서와 발표 전제 |
| `../008-presentation/midterm-presentation-guide.md` | 중간 발표 메시지와 시연 구조 |

규칙:

- 혼자 바로 바꾸지 않고 Issue, PR 코멘트, 팀 논의 중 하나로 변경 이유를 먼저 공유한다.
- 변경 시 관련 문서(`service-flow`, `prd`, `trd`, `api-contract`)와 충돌이 없는지 같이 확인한다.
- 서비스 핵심 방향이 바뀌면 daily report가 아니라 별도 문서 PR로 올린다.

## 공통 문서 양식

모든 문서는 제목 바로 아래에 아래 6개 메타데이터를 같은 순서로 둔다.

```md
# 문서 제목

> 문서 유형: PRD / TRD / WBS / ADR / Guide / API Contract 등
> 상태: Draft / Review / Approved / Deprecated
> 정본 위치: `docs/...`
> 관련 문서: `docs/...` 또는 `-`
> 버전: v0.1
> 최종 수정: YYYY-MM-DD
```

## 상태 관리 기준

| 상태 | 의미 | 변경 기준 |
|---|---|---|
| Draft | 작성 중인 초안 | 최초 작성, 정책/구조가 아직 팀 합의 전인 상태 |
| Review | 팀 검토 중 | PR 또는 회의에서 팀원이 검토하기 시작한 상태 |
| Approved | 기준 문서로 확정 | 팀 합의가 끝나고 구현/발표 기준으로 사용하기로 한 상태 |
| Deprecated | 더 이상 사용하지 않음 | 다른 문서로 대체되었거나 내용이 폐기된 상태 |

현재 문서들은 아직 팀 리뷰 전이므로 기본 상태를 `Draft`로 둔다. PR 리뷰나 팀 회의에서 기준으로 쓰기로 합의되면 `Review`를 거쳐 `Approved`로 변경한다.

## 버전 관리 기준

문서 버전은 `v{major}.{minor}` 형식을 사용한다.

| 변경 | 기준 |
|---|---|
| 새 문서 | `v0.1`로 시작 |
| `v0.x` | 발표 전 Draft/Review 단계 문서 |
| minor 증가 | 기능 범위, API, DB, WBS, 문서 운영 방식처럼 팀 작업에 영향을 주는 내용 변경 |
| major 증가 | 발표/배포 기준 문서가 확정되거나 서비스 방향이 크게 바뀌는 경우 |
| 단순 수정 | 오타, 띄어쓰기, 링크 수정처럼 의미 변화가 없으면 버전 유지 가능 |

MVP 발표 전까지는 `v0.1`부터 시작하고, 팀 리뷰로 기준이 확정되면 `v0.2`, 최종 발표 기준으로 잠그면 `v1.0`으로 올린다.

### 버전/최종 수정일 갱신 규칙

문서 본문을 수정할 때는 아래 기준을 따른다.

| 상황 | 버전 | 최종 수정 |
|---|---|---|
| 새 문서 작성 | `v0.1` | 작성일 |
| 팀 작업 기준 변경 | minor 증가 | 변경일 |
| API/DB/권한/상태값/일정 변경 | minor 증가 | 변경일 |
| daily report, Git 전략, 문서 수정 권한 같은 운영 방식 변경 | minor 증가 | 변경일 |
| 오타/표현/링크만 수정 | 유지 가능 | 변경일 또는 유지 가능 |
| 발표/배포 기준으로 확정 | `v1.0` | 확정일 |

규칙:

- 본문 의미가 바뀌는 PR은 해당 문서의 `최종 수정`을 PR 작성일 기준으로 갱신한다.
- 팀 작업 방식, 일정, API, DB, 권한, 서비스 흐름에 영향을 주면 minor 버전을 올린다.
- 여러 문서를 함께 바꾸면 실제로 내용이 바뀐 문서만 버전/최종 수정일을 갱신한다.
- `template.md`처럼 새 파일 생성용 placeholder가 있는 문서는 템플릿 필드(`YYYY-MM-DD`)를 유지할 수 있다.
- 버전/최종 수정일을 갱신하지 않은 문서 변경은 PR 리뷰에서 수정 요청 대상이 될 수 있다.
