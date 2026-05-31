# Daily Report Guide

> 문서 유형: Daily Report Guide
> 상태: Draft
> 정본 위치: `docs/006-planning/daily-reports/guide.md`
> 관련 문서: `docs/006-planning/daily-reports/template.md`, `docs/006-planning/daily-discussions/discussion-guide.md`, `docs/010-development/agent-workflow.md`
> 버전: v0.2
> 최종 수정: 2026-05-31

이 폴더는 팀원별 하루 작업 보고서를 관리한다.
목적은 각자의 작업 기록을 GitHub PR로 남기되, 같은 파일을 여러 명이 수정해서 충돌나는 일을 줄이는 것이다.
파일은 날짜별 폴더 아래에 담당자 이름으로 나눈다.

## 1. 폴더 구조

근무일별 폴더를 만들고, 그 안에 팀원별 파일을 둔다.

```text
docs/006-planning/daily-reports/YYYY-MM-DD/
├─ min-jungki.md
├─ kim-jinhyeok.md
├─ lee-seuli.md
├─ hwang-heesoo.md
└─ kim-gayeong.md
```

예시:

```text
docs/006-planning/daily-reports/2026-06-01/kim-jinhyeok.md
```

## 2. 작성 기준

각 팀원은 본인 daily report 파일만 수정한다.
각자의 에이전트는 작업 내용을 아래 항목으로 분류하고, 팀 합의가 필요한 내용은 `다음 근무일 논의` 또는 `API/DB/화면 영향`에 표시한다.

| 항목 | 기준 |
|---|---|
| 완료 | 실제로 끝낸 작업, 리뷰한 PR, 합의된 결정 |
| 미완료 | 구현/문서/리뷰가 남은 작업 |
| 다음 근무일 논의 | 다른 담당자와 맞춰야 하는 내용 |
| API/DB/화면 영향 | API 계약, DB migration, 화면/상태값에 영향이 있는 내용 |
| 링크 | 관련 Issue, PR, 커밋, Figma 링크 |

## 3. PR 기준

daily report는 개인별 PR로 올린다.

```text
브랜치: docs/daily-YYYY-MM-DD-name
PR 제목: docs: YYYY-MM-DD 이름 daily report 작성
```

예시:

```text
docs/daily-2026-06-01-lee-seuli
docs: 2026-06-01 이슬이 daily report 작성
```

규칙:

- 본인 daily report 파일만 수정한다.
- 날짜별 폴더가 없으면 먼저 만들고, 5명 파일을 템플릿 기준으로 준비한다.
- 기능 코드, API 계약, DB migration은 daily report PR에 섞지 않는다.
- 기능 변경은 별도 `feat/*`, `fix/*`, `docs/*` PR로 올린다.
- daily report가 merge되면 다음 근무일 아침 Codex가 이 문서를 참고해 할 일을 안내한다.

## 4. Codex 요청 문장

작업 종료 시:

```text
나 [이름]인데 오늘 한 거 daily report로 정리해줘.
완료, 미완료, 다음 근무일 논의, API/DB/화면 영향, 관련 PR/Issue 기준으로 작성해줘.
파일 위치는 docs/006-planning/daily-reports/YYYY-MM-DD/[name].md 로 해줘.
```

다음 근무일 시작 시:

```text
나 [이름]인데 오늘 뭐해?
daily-plans/YYYY-MM-DD.md, weekly-wbs, member-wbs, 전날 daily-reports를 기준으로 오늘 할 일을 정리해줘.
```

## 5. daily-discussions와의 차이

| 구분 | daily-reports | daily-discussions |
|---|---|---|
| 목적 | 개인별 하루 작업 보고 | 팀 논의/결정사항 정리 |
| 작성 주기 | 근무일마다 개인별 작성 | 필요할 때 작성 |
| 작성 단위 | 사람별 파일 | 날짜별 또는 안건별 파일 |
| PR 방식 | 개인별 PR | 정리 담당 PR |

daily report만으로 충분한 날에는 `daily-discussions`를 만들지 않는다.
API/DB/화면/정책 합의가 필요한 날에만 `daily-discussions`를 작성한다.

## 6. 에이전트가 판단해야 할 것

팀원이 "오늘 한 거 정리해줘"라고 요청하면 에이전트는 단순 요약만 하지 않는다.
아래 기준으로 다음 액션까지 분류한다.

| 판단 | 에이전트 행동 |
|---|---|
| 개인 작업만 정리하면 됨 | daily report만 작성 |
| 다른 담당자와 맞춰야 함 | `다음 근무일 논의`에 후보로 기록 |
| API/DB/화면/권한 변경 가능성 있음 | `API/DB/화면 영향`에 명시 |
| 팀 결정이 필요해 보임 | daily report에 `daily discussion 후보`라고 적음 |

daily discussion 파일을 실제로 만들지 여부는 정리 담당이 결정한다.
