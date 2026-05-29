# Daily Discussion Guide

> 문서 유형: Daily Discussion Guide
> 상태: Draft
> 정본 위치: `docs/006-planning/daily-discussions/discussion-guide.md`
> 관련 문서: `docs/006-planning/wbs.md`, `docs/006-planning/daily-work-plan.md`, `docs/006-planning/weekly-wbs/2026-06-01-week1.md`
> 버전: v0.1
> 최종 수정: 2026-05-29

이 폴더는 하루 작업 종료 시 **다음 근무일에 논의할 사항**을 남기는 곳이다.
목적은 매일 아침 회의를 짧게 만들고, API/DB/화면 변경이 다른 담당자에게 늦게 전달되는 것을 막는 것이다.

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
→ 완료/미완료/다음 근무일 논의/API·DB·화면 영향 공유
→ 다음 근무일 daily-discussions/YYYY-MM-DD.md에 정리
```

금요일에는 다음 월요일 논의 파일을 작성한다.

## 1. 파일명 규칙

다음 근무일 날짜로 파일을 만든다.

```text
YYYY-MM-DD.md
```

예시:

```text
2026-06-02.md
2026-06-03.md
2026-06-08.md
```

금요일 작업 종료 시에는 다음 월요일 날짜로 파일을 만든다.

## 2. 작성 시점

- 매일 작업 종료 전 10분
- PR을 올리기 전 API/DB/화면 영향이 생겼을 때
- 다음날 오전에 팀 합의가 필요한 이슈가 생겼을 때

## 3. 작성 원칙

- 길게 회고하지 말고, 다음날 결정해야 하는 것만 적는다.
- API 변경은 `docs/004-api/api-contract.md` 반영 여부를 함께 적는다.
- DB 변경은 migration 필요 여부를 함께 적는다.
- 프론트 영향은 황희수가 바로 판단할 수 있도록 화면/버튼/상태값 기준으로 적는다.
- 결정이 끝난 항목은 해당 일자 파일에서 체크 표시한다.

## 4. 템플릿

새 파일을 만들 때 `template.md`를 복사해서 사용한다.

## 5. 팀원 종료 메모 형식

각 담당자는 작업 종료 전 아래 형식으로 메모를 남긴다.

```text
담당자:
완료:
미완료:
다음 근무일 논의:
API/DB/화면 영향:
```

이 메모를 모아 다음 근무일 `daily-discussions/YYYY-MM-DD.md`에 담당자별 논의사항으로 정리한다.
