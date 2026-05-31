# Daily Plan Guide

> 문서 유형: Planning Guide
> 상태: Draft
> 정본 위치: `docs/006-planning/daily-plans/guide.md`
> 관련 문서: `docs/006-planning/weekly-wbs/`, `docs/006-planning/daily-reports/guide.md`, `docs/006-planning/daily-discussions/discussion-guide.md`
> 버전: v0.1
> 최종 수정: 2026-05-31

Daily plan은 팀원이 아침에 "나 오늘 뭐하면 돼?"라고 물었을 때 보는 날짜별 실행 문서다.
기존 `today.md`처럼 매일 덮어쓰지 않고, 날짜별 파일로 남겨 히스토리를 보존한다.

## 파일 위치

```text
docs/006-planning/daily-plans/YYYY-MM-DD.md
```

예시:

```text
docs/006-planning/daily-plans/2026-06-01.md
```

## 작성 시점

Daily plan은 전날 퇴근 전 또는 당일 오전 시작 전에 작성한다.

작성 기준:

- 전날 개인 daily report
- 필요 시 daily discussion
- 이번 주 weekly WBS
- 개인별 member WBS
- 전날 PR/Issue 상태

daily discussion이 없는 날은 전날 daily report와 weekly WBS만 보고 작성한다.
daily discussion 파일이 없다는 것은 "논의가 누락되었다"가 아니라 "팀 합의가 필요한 안건이 없었다"는 의미다.

## 작성 흐름

```text
개인 daily-reports 확인
-> 결정 필요한 내용이 있으면 daily-discussions 확인/작성
-> weekly-wbs에서 이번 주 목표 확인
-> 다음 근무일 daily-plans/YYYY-MM-DD.md 작성
-> 팀원은 해당 파일을 보고 작업 시작
```

## daily report와의 관계

| 문서 | 역할 | 작성 단위 |
|---|---|---|
| `daily-plans/YYYY-MM-DD.md` | 오늘 할 일 | 날짜별 1개 |
| `daily-reports/YYYY-MM-DD/{member}.md` | 오늘 실제 한 일 | 날짜별/사람별 |
| `daily-discussions/YYYY-MM-DD.md` | 팀 결정/논의 | 필요한 날만 |

## 운영 규칙

- daily plan은 날짜별로 새 파일을 만든다.
- 이전 daily plan을 덮어쓰지 않는다.
- 팀원별 오늘 할 일은 한 줄로 명확하게 적는다.
- API/DB/화면 영향이 있는 작업은 별도 섹션에 표시한다.
- 종료 후에는 각자 daily report를 작성한다.

## Codex 요청 예시

```text
내일 daily plan 만들어줘.
오늘 daily-reports, daily-discussions, weekly-wbs 기준으로
담당자별 할 일, 논의사항, API/DB/화면 영향을 정리해줘.
파일은 docs/006-planning/daily-plans/YYYY-MM-DD.md 로 만들어줘.
```
