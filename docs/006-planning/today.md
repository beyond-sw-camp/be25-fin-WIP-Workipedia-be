# Today — 2026-05-29

> 문서 유형: Daily Entry Point
> 상태: Draft
> 정본 위치: `docs/006-planning/today.md`
> 관련 문서: `docs/006-planning/weekly-wbs/2026-06-01-week1.md`, `docs/006-planning/daily-discussions/2026-06-01.md`, `docs/006-planning/daily-discussions/discussion-guide.md`
> 버전: v0.1
> 최종 수정: 2026-05-29

팀원이 "나 오늘 뭐하면 돼?"라고 물으면 이 문서를 먼저 확인한다.
`today.md`는 매일 갱신하는 운영 진입점이며, 상세 작업은 주간 WBS와 개인별 WBS를 기준으로 한다.

## 1. 오늘 공통 작업

| 순서 | 작업 | 완료 기준 |
|---:|---|---|
| 1 | `dev` 기준 최신 코드 pull | 로컬 작업 브랜치가 최신 기준에서 시작됨 |
| 2 | PR #10 문서 구조 확인 | `docs/001-reference`, `docs/006-planning` 구조 이해 |
| 3 | 본인 담당 문서 확인 | 개인별 WBS와 Week 1 WBS에서 담당 작업 확인 |
| 4 | 월요일 시작 전 막히는 점 공유 | 2026-06-01 오전 논의사항에 반영 |

## 2. 담당자별 오늘 할 일

| 담당자 | 오늘 할 일 | 확인할 문서 |
|---|---|---|
| 민정기 | 워키/FAQ/알림 범위와 월요일 skeleton 작업 확인 | `docs/006-planning/member-wbs/min-jungki.md`, `docs/006-planning/weekly-wbs/2026-06-01-week1.md` |
| 김가영 | 휴가 복귀 후 월요일에 받을 입력값 확인 | `docs/006-planning/member-wbs/kim-gayeong.md`, `docs/006-planning/daily-discussions/2026-06-01.md` |
| 김진혁 | 티켓 상태값, 챗봇 실패 처리, RAG references 구조 확인 | `docs/006-planning/member-wbs/kim-jinhyeok.md`, `docs/001-reference/service-flow.md` |
| 이슬이 | Auth 필드, 권한 구조, 챗봇 세션/메시지 저장 경계 확인 | `docs/006-planning/member-wbs/lee-seuli.md`, `docs/003-adr/0003-auth-strategy.md` |
| 황희수 | Figma Make 화면 목록, 라우팅, mock API 필요 목록 확인 | `docs/006-planning/member-wbs/hwang-heesoo.md`, `docs/004-api/api-contract.md` |

## 3. 팀원이 "나 오늘 뭐하면 돼?"라고 물었을 때

아래 순서로 안내한다.

```text
1. docs/006-planning/today.md 확인
2. 본인 member-wbs 확인
3. 이번 주 weekly-wbs 확인
4. 오늘 작업 종료 전 완료/미완료/내일 논의/API·DB·화면 영향 공유
```

예시:

```text
이슬이는 today.md에서 본인 행을 먼저 보고,
member-wbs/lee-seuli.md와 weekly-wbs/2026-06-01-week1.md를 확인한 뒤
Auth/챗봇 세션 skeleton을 월요일에 바로 시작할 수 있게 경계를 정리한다.
```

## 4. 오늘 종료 전 받을 메모

각 담당자는 정해진 양식을 맞출 필요 없이 오늘 한 일을 자연어로 공유한다.
정리 담당이 이 내용을 `완료`, `미완료`, `다음 근무일 논의`, `API/DB/화면 영향`으로 분류한다.

```text
오늘 한 거 정리해서 보내줘.
막힌 거나 월요일에 같이 정해야 할 것도 같이 적어줘.
```

## 5. 다음 근무일 논의 파일

오늘은 금요일이므로 다음 근무일 논의 파일은 아래 문서다.

```text
docs/006-planning/daily-discussions/2026-06-01.md
```

업무 종료 시 팀원 메모를 모아 위 파일에 담당자별 논의 내용을 추가한다.
