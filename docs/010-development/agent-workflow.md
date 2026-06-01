# Agent Workflow — Workipedia

> 문서 유형: Agent Workflow
> 상태: Draft
> 정본 위치: `docs/010-development/agent-workflow.md`
> 관련 문서: `docs/001-reference/document-guide.md`, `docs/006-planning/daily-plans/YYYY-MM-DD.md`, `docs/006-planning/daily-reports/guide.md`, `docs/006-planning/definition-of-done.md`
> 버전: v0.2
> 최종 수정: 2026-05-31

본 문서는 팀원이 Codex/Claude 같은 에이전트와 작업할 때 사용하는 공통 절차를 정의한다.
목적은 에이전트가 같은 문서를 읽고, 같은 기준으로 작업을 제안하고, 같은 형식으로 보고서를 남기게 하는 것이다.

## 1. 작업 시작 절차

팀원은 작업 시작 시 아래 문장으로 시작한다.

```text
나 [이름]인데 오늘 뭐해?
daily-plans/YYYY-MM-DD.md, weekly-wbs, member-wbs, 전날 daily-reports를 기준으로 오늘 할 일을 정리해줘.
내가 수정해도 되는 문서와 리뷰가 필요한 문서도 같이 알려줘.
```

에이전트는 아래 문서를 우선 확인한다.

1. `docs/006-planning/daily-plans/YYYY-MM-DD.md`
2. `docs/006-planning/weekly-wbs/`
3. `docs/006-planning/member-wbs/{member}.md`
4. `docs/006-planning/daily-reports/`
5. `docs/001-reference/document-guide.md`

## 2. 작업 전 확인

기능 개발에 들어가기 전 에이전트는 아래를 확인한다.

- 관련 Issue가 있는가
- 현재 브랜치가 `dev`가 아닌 작업 브랜치인가
- 작업 범위가 WBS와 맞는가
- API/DB/화면 영향이 있는가
- 수정 가능한 문서인지, 리뷰가 필요한 문서인지

기능 작업 브랜치 예시:

```text
feat/auth-jwt
feat/ticket-local-rag
feat/worki-question
feat/frontend-core-flow
```

daily report 브랜치 예시:

```text
docs/daily-2026-06-01-lee-seuli
```

## 3. 작업 중 원칙

- 루트 `README.md`는 수정하지 않는다.
- 개인 daily report와 본인 member WBS 외의 문서는 영향 범위를 먼저 확인한다.
- API 계약이 바뀌면 `docs/004-api/api-contract.md`를 함께 본다.
- DB가 바뀌면 `docs/005-database/db-migration-guide.md`와 migration 여부를 함께 본다.
- 서비스 흐름이 바뀌면 `docs/001-reference/service-flow.md`를 먼저 확인한다.
- 완료 판단은 `docs/006-planning/definition-of-done.md`를 따른다.

## 4. 작업 종료 절차

팀원은 작업 종료 시 아래 문장으로 daily report를 만든다.

```text
나 [이름]인데 오늘 한 거 daily report로 정리해줘.
완료, 미완료, 다음 근무일 논의, API/DB/화면 영향, 관련 PR/Issue 기준으로 작성해줘.
파일 위치는 docs/006-planning/daily-reports/YYYY-MM-DD/[name].md 로 해줘.
```

daily report는 본인 파일만 PR로 올린다.

```text
PR 제목: docs: YYYY-MM-DD 이름 daily report 작성
```

## 5. 다음 근무일 논의 정리

daily report가 여러 개 merge된 뒤, 정리 담당은 필요할 때만 `daily-discussions`를 만든다.
현재 정리 담당은 김진혁으로 둔다.

각 팀원의 에이전트는 daily report 작성 시 논의 후보를 표시한다.
정리 담당의 에이전트는 여러 daily report를 모아보고 daily discussion 생성 여부를 판단한다.

```text
docs/006-planning/daily-reports/YYYY-MM-DD/ 아래 보고서를 보고
다음 근무일에 먼저 논의할 안건, 담당자별 확인사항, API/DB/화면 충돌 가능성을 정리해줘.
필요하면 docs/006-planning/daily-discussions/YYYY-MM-DD.md로 작성해줘.
```

`daily-discussions`는 매일 필수 문서가 아니라 팀 합의가 필요한 날 작성하는 논의 문서다.
논의할 내용이 없으면 파일을 만들지 않고, 다음 daily plan에 각자 할 일만 반영한다.

## 6. PR 전 확인 문장

PR을 올리기 전 에이전트에게 아래처럼 요청한다.

```text
이 변경사항 PR 올리기 전에 확인해줘.
기능 변경, 문서 변경, API/DB/화면 영향, 수정하면 안 되는 파일 포함 여부,
Definition of Done 충족 여부를 기준으로 리뷰해줘.
```

에이전트는 아래를 확인한다.

- 기능 PR과 daily report PR이 섞였는가
- API 계약 문서가 필요한데 빠졌는가
- DB migration 문서가 필요한데 빠졌는가
- 문서 수정 권한 기준을 어겼는가
- 하네스/보안/권한 체크가 필요한가

## 7. 팀원별 시작 문장

```text
나 민정기인데 오늘 뭐해?
나 김진혁인데 오늘 뭐해?
나 이슬이인데 오늘 뭐해?
나 황희수인데 오늘 뭐해?
나 김가영인데 오늘 뭐해?
```

## 8. 금지 사항

- `dev`에서 직접 기능 커밋하지 않는다.
- 루트 `README.md`를 임의 수정하지 않는다.
- 다른 사람 daily report를 임의 수정하지 않는다.
- API/DB/권한 변경을 문서 없이 구현하지 않는다.
- 출처 없는 챗봇 답변을 완료로 보지 않는다.
