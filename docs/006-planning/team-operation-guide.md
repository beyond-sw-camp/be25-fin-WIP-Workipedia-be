# Team Operation Guide — Workipedia

> 문서 유형: Team Operation Guide
> 상태: Draft
> 정본 위치: `docs/006-planning/team-operation-guide.md`
> 관련 문서: `docs/006-planning/wbs.md`, `docs/006-planning/weekly-wbs/`, `docs/006-planning/daily-plans/`, `docs/006-planning/daily-reports/`, `docs/006-planning/daily-discussions/`, `docs/009-process/git-strategy.md`
> 버전: v0.1
> 최종 수정: 2026-05-31

김진혁이 팀 운영을 놓치지 않기 위한 하루 단위 운영 가이드다.
개발 내용이 아니라, WBS/Daily Plan/Daily Report/Daily Discussion/Docs PR을 어떻게 굴릴지 정리한다.

## 1. 핵심 흐름

```text
Weekly WBS
-> Daily Plan
-> GitHub Issue
-> 작업
-> Daily Report
-> Daily Discussion
-> Daily Docs PR
```

| 문서 | 역할 | 작성 시점 |
|---|---|---|
| Weekly WBS | 주간 작업 범위와 담당자별 목표 | 금요일 또는 주 시작 전 |
| Daily Plan | 오늘 할 일과 확인 기준 | 매일 아침 또는 전날 밤 |
| Daily Report | 오늘 각자 한 일, 미완료, 영향 범위 | 매일 퇴근 전 |
| Daily Discussion | 다음 날 팀이 결정해야 할 안건 | daily report 취합 후, 필요 시 |
| Daily Docs PR | 하루 운영 문서 공유 | 매일 작업 종료 후 |

## 2. 아침 운영

아침에 김진혁이 확인할 것:

```text
1. dev 최신화 안내
2. 오늘 daily plan 존재 여부 확인
3. weekly-wbs와 member-wbs 기준으로 담당자별 오늘 할 일 확인
4. 작업 시작 전 GitHub Issue 생성 안내
5. DB/API/화면 변경 시 daily report에 영향 범위 기록 안내
```

팀원에게 공지할 말:

```text
오늘 작업은 WBS -> Daily Plan -> Issue -> 작업 -> Daily Report 흐름으로 갑니다.
아침에 dev pull 받고, Codex에게 "나 [이름]인데 오늘 뭐하면 돼? daily plan이랑 내 WBS 기준으로 알려줘."라고 물어보세요.
작업 시작 전에는 GitHub Issue 먼저 만들고, 기능 브랜치에서 작업하면 됩니다.
```

## 3. 팀원별 시작 안내

| 담당자 | 아침에 확인할 것 |
|---|---|
| 민정기 | 워키/FAQ/알림 담당 범위, 워키 관련 Issue 생성 |
| 김가영 | handoff 문서, 관리자/포인트/ESG 등급/지표 담당 범위, 휴가 중 변경사항 |
| 김진혁 | 티켓/챗봇 실패 전환/RAG 출처 저장 구조, 팀 운영 문서 |
| 이슬이 | Auth/JWT/Refresh Token/챗봇 세션 담당 범위, Secure Cookie 로컬 정책 |
| 황희수 | Figma Make와 최신 기획 차이, 질문/요청 분리 화면, mock API 기준 |

## 4. 작업 중 확인

김진혁이 중간에 확인할 체크리스트:

```text
- API 변경이 docs/004-api/api-contract.md에 반영되어야 하는가?
- DB 변경이 V2 이후 migration으로 가야 하는가?
- FE 화면 영향이 황희수에게 공유되었는가?
- 공통 응답 형식(code/status/message/data)이 유지되는가?
- 팀원이 Issue 없이 기능 개발을 시작하지 않았는가?
```

DB migration 기준:

```text
현재 DB는 V1__create_initial_schema.sql 통합 초안 기준이다.
이 V1이 공유되거나 PR에 올라가거나 dev에 merge된 뒤에는 수정하지 않는다.
이후 DB 변경은 V2, V3처럼 새 migration으로 추가한다.
```

## 5. 퇴근 전 운영

퇴근 전 팀원에게 공지할 말:

```text
오늘 한 작업을 Codex에게 정리시켜서 본인 daily report에 남겨주세요.
말은 자연어로 해도 되고, Codex가 완료/미완료/논의/API·DB·화면 영향으로 정리하면 됩니다.
```

팀원이 Codex에게 말할 문장:

```text
나 [이름]인데 오늘 한 거 daily report로 정리해줘.
완료한 것, 미완료, 막힌 점, API/DB/화면 영향, 내일 논의할 것을 정리해줘.
```

김진혁이 확인할 것:

```text
1. 팀원별 daily report가 작성됐는가?
2. 미완료/막힌 점이 정리됐는가?
3. 다음 날 논의할 내용이 daily discussion에 들어갔는가?
4. 다음 날 daily plan 초안이 필요한가?
5. daily docs PR을 올렸는가?
```

## 6. Daily Docs PR 규칙

매일 운영 문서는 하루 단위 docs PR로 공유한다.

```text
브랜치명:
docs/daily-YYYY-MM-DD

예시:
docs/daily-2026-06-01

커밋 메시지:
docs: YYYY-MM-DD daily 정리

PR 제목:
docs: YYYY-MM-DD daily 정리
```

PR에 포함할 수 있는 문서:

```text
docs/006-planning/daily-reports/YYYY-MM-DD/{member}.md
docs/006-planning/daily-discussions/YYYY-MM-DD.md
docs/006-planning/daily-plans/YYYY-MM-DD.md
```

기능 코드, API 계약 변경, DB migration, 화면 구현은 daily docs PR에 섞지 않는다.
그런 변경은 별도 feature/docs PR로 올린다.

## 7. 김진혁이 Codex에게 시킬 말

아침:

```text
나 김진혁이고 오늘 팀 운영 시작해야 해.
weekly-wbs, member-wbs, daily-plan, daily-discussion 기준으로
팀원별 오늘 할 일과 내가 확인할 체크리스트 정리해줘.
```

저녁:

```text
오늘 팀원 daily report 기준으로 내일 daily discussion 만들어줘.
미완료, API/DB/화면 영향, 결정 필요한 안건 중심으로 정리해줘.
그리고 필요하면 내일 daily plan 초안도 만들어줘.
```

주간 WBS 작성:

```text
이번 주 daily reports와 weekly-wbs 진행 상황 기준으로
다음 주 WBS 초안 만들어줘.
담당자별 월-금 작업, 완료 기준, 논의사항을 포함해줘.
```

## 8. 기억할 것

```text
아침: Daily Plan 기준으로 오늘 할 일 뿌리기
작업 전: Issue 만들게 하기
저녁: Daily Report 작성하게 하기
마감: Daily Discussion + Daily Docs PR 올리기
금요일: 다음 주 WBS 정리하기
```
