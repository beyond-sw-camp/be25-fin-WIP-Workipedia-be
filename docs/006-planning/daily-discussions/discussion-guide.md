# Daily Discussion Guide

> 문서 유형: Daily Discussion Guide
> 상태: Draft
> 정본 위치: `docs/006-planning/daily-discussions/discussion-guide.md`
> 관련 문서: `docs/006-planning/wbs.md`, `docs/006-planning/daily-work-plan.md`, `docs/006-planning/weekly-wbs/2026-06-01-week1.md`
> 버전: v0.2
> 최종 수정: 2026-05-31

이 폴더는 daily report를 바탕으로 **팀 합의가 필요한 논의사항과 결정사항**을 남기는 곳이다.
개인별 하루 작업 보고는 `docs/006-planning/daily-reports/`에 작성하고, 이 폴더는 논의가 필요한 날에만 작성한다.

## 0. 운영 흐름

팀원이 "나 오늘 뭐하면 돼?"라고 물으면 `docs/006-planning/today.md`를 먼저 확인한다.
`today.md`는 매일 갱신되는 진입점이고, 실제 상세 작업은 주간 WBS와 개인별 WBS를 따른다.

```text
아침
→ today.md 확인
→ 본인 member-wbs 확인
→ weekly-wbs 확인
→ 작업 진행

퇴근 전
→ 팀원이 본인 daily report 작성
→ docs/006-planning/daily-reports/YYYY-MM-DD/{member}.md PR 생성
→ daily report가 dev에 merge됨
→ 정리 담당이 필요 시 daily-discussions/YYYY-MM-DD.md에 논의사항 정리
```

금요일에는 다음 월요일에 논의할 안건이 있을 때만 daily discussion 파일을 작성한다.

## 0.1 daily report와 daily discussion 구분

`daily-reports`는 근무일마다 개인별로 작성한다.
`daily-discussions`는 매일 필수가 아니라 팀 논의나 결정이 필요한 경우에만 작성한다.

| 구분 | daily-reports | daily-discussions |
|---|---|---|
| 목적 | 개인별 하루 작업 보고 | 팀 논의/결정사항 정리 |
| 작성 주기 | 근무일마다 개인별 작성 | 필요할 때 작성 |
| 작성 단위 | 사람별 파일 | 날짜별 또는 안건별 파일 |
| PR 방식 | 개인별 PR | 정리 담당 PR |

개인 daily report는 `docs/006-planning/daily-reports/guide.md` 기준을 따른다.
기능 개발, API 변경, DB 변경, 화면 구현은 daily report 또는 daily discussion PR에 섞지 않는다.

## 1. 파일명 규칙

논의가 필요한 날짜 또는 안건 기준으로 파일을 만든다.

```text
YYYY-MM-DD.md
YYYY-MM-DD-topic.md
```

예시:

```text
2026-06-02.md
2026-06-03-api-contract.md
2026-06-08-week2-planning.md
```

금요일 작업 종료 시에도 논의할 안건이 없다면 파일을 만들지 않는다.

## 2. 작성 시점

- 여러 팀원의 daily report에서 같은 논의사항이 반복될 때
- API/DB/화면 영향이 여러 담당자에게 걸려 있을 때
- 다음날 오전에 팀 합의가 필요한 이슈가 생겼을 때
- 정책, 역할, 권한, 서비스 흐름 변경이 필요할 때

## 3. 작성 원칙

- 개인 회고가 아니라 팀이 결정해야 하는 것만 적는다.
- API 변경은 `docs/004-api/api-contract.md` 반영 여부를 함께 적는다.
- DB 변경은 migration 필요 여부를 함께 적는다.
- 프론트 영향은 황희수가 바로 판단할 수 있도록 화면/버튼/상태값 기준으로 적는다.
- 결정이 끝난 항목은 해당 일자 파일에서 체크 표시한다.

## 4. 템플릿

새 파일을 만들 때 `template.md`를 복사해서 사용한다.

## 5. daily report 기반 정리 방식

팀원은 본인 daily report를 작성한다.
정리 담당은 merge된 daily report들을 보고 논의사항만 추린다.

예시:

```text
docs/006-planning/daily-reports/2026-06-01/lee-seuli.md
docs/006-planning/daily-reports/2026-06-01/kim-jinhyeok.md
```

정리 담당은 위 보고서들을 보고 아래 항목으로 분류한다.

| 항목 | 정리 기준 |
|---|---|
| 완료 | 이미 합의되었거나 결정된 내용 |
| 미완료 | 논의가 끝나지 않은 내용 |
| 다음 근무일 논의 | 팀 합의가 필요하거나 담당자 간 조율이 필요한 내용 |
| API/DB/화면 영향 | API 계약, DB migration, 프론트 화면에 영향을 주는 내용 |

daily report만으로 충분한 날에는 daily discussion을 만들지 않는다.
