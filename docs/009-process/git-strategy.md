# Git Strategy

> 문서 유형: Process Guide
> 상태: Draft
> 정본 위치: `docs/009-process/git-strategy.md`
> 관련 문서: `docs/006-planning/wbs.md`, `docs/004-api/api-contract.md`, `docs/005-database/db-migration-guide.md`
> 버전: v0.1
> 최종 수정: 2026-05-28

## 1. 목적

팀원이 같은 기준으로 브랜치를 만들고, PR을 올리고, 충돌을 줄이기 위한 Git 협업 전략이다.

이 프로젝트는 발표 일정이 빠르고 담당 영역이 겹치므로, 작은 PR과 명확한 브랜치 이름을 우선한다.

## 2. 브랜치 전략 요약

이 프로젝트는 발표 일정이 짧고 팀원이 5명이므로, 무거운 Git Flow를 그대로 쓰지 않는다. 대신 **단순화된 Git Flow**를 사용한다.

```text
main
  ↑
release/2026-06-26
  ↑
develop
  ↑
feature branches
```

기본 원칙:

- `main`은 발표/배포 가능한 안정 버전만 둔다.
- 실제 개발 통합은 `develop`에서 한다.
- 각 기능은 `feat/*` 브랜치에서 작업한다.
- 배포 직전에는 `release/2026-06-26` 브랜치를 만들어 기능 추가를 막고 안정화한다.
- 발표 직전 급한 수정은 `hotfix/*` 브랜치에서 처리한다.

현재 브랜치가 `chore/init-setting`이라면, 이 브랜치에 초기 설정과 문서 작업을 모은 뒤 팀 합의로 `develop`을 새로 만들거나 `chore/init-setting`을 `develop`에 병합한다.

## 3. 기본 브랜치

| 브랜치 | 역할 |
|---|---|
| `main` | 최종 발표/배포 기준 브랜치. 직접 작업 금지 |
| `develop` | 통합 개발 브랜치. 기능 PR이 모이는 곳 |
| `release/2026-06-26` | 배포 후보 브랜치. 2026-06-25 전후 생성 |
| `feat/*` | 기능 개발 브랜치 |
| `fix/*` | 일반 버그 수정 브랜치 |
| `hotfix/*` | 배포/발표 직전 긴급 수정 브랜치 |
| `docs/*` | 문서 작업 브랜치 |
| `chore/*` | 설정, 빌드, 환경 작업 브랜치 |

추천 흐름:

```text
feat/auth-jwt -> develop
feat/worki-question -> develop
feat/local-rag -> develop
develop -> release/2026-06-26
release/2026-06-26 -> main
hotfix/demo-login -> release/2026-06-26 -> main
```

## 4. 브랜치 네이밍

```text
{type}/{scope}-{short-description}
```

| type | 용도 | 예시 |
|---|---|---|
| `feat` | 기능 추가 | `feat/auth-login` |
| `fix` | 버그 수정 | `fix/ticket-status` |
| `docs` | 문서 | `docs/api-contract` |
| `chore` | 설정/빌드/환경 | `chore/flyway-setup` |
| `refactor` | 동작 변화 없는 구조 개선 | `refactor/chatbot-service` |
| `test` | 테스트 추가/수정 | `test/worki-policy` |

담당자별 예시:

| 담당 | 브랜치 예시 |
|---|---|
| 민정기 | `feat/worki-question`, `feat/notification-sse` |
| 김가영 | `feat/admin-dashboard`, `feat/point-badge` |
| 김진혁 | `feat/ticket-transfer`, `feat/local-rag` |
| 이슬이 | `feat/auth-jwt`, `feat/chatbot-session` |
| 황희수 | `feat/frontend-chatbot`, `feat/frontend-admin` |

## 5. 담당자별 초기 브랜치 추천

| 담당 | 1차 브랜치 | 작업 |
|---|---|---|
| 민정기 | `feat/worki-faq-notification` | 워키, FAQ, 알림 |
| 김가영 | `feat/admin-point-badge-esg` | 관리자, 포인트, 뱃지, ESG |
| 김진혁 | `feat/ticket-local-rag` | 티켓, 이관, local RAG |
| 이슬이 | `feat/auth-chatbot-session` | Auth, 챗봇 세션/메시지 |
| 황희수 | `feat/frontend-core-flow` | 로그인, 챗봇, 워키, 티켓, 관리자 화면 |

1차 브랜치가 너무 커지면 아래처럼 쪼갠다.

| 큰 브랜치 | 분리 예시 |
|---|---|
| `feat/worki-faq-notification` | `feat/worki-question`, `feat/faq-summary`, `feat/notification-sse` |
| `feat/admin-point-badge-esg` | `feat/admin-dashboard`, `feat/point-badge`, `feat/esg-metrics` |
| `feat/ticket-local-rag` | `feat/ticket-transfer`, `feat/local-rag`, `feat/chatbot-escalation` |
| `feat/auth-chatbot-session` | `feat/auth-jwt`, `feat/chatbot-session` |
| `feat/frontend-core-flow` | `feat/frontend-auth`, `feat/frontend-chatbot`, `feat/frontend-admin` |

## 6. 커밋 메시지

```text
{type}: {변경 요약}
```

예시:

```text
feat: 로그인 API 추가
feat: 워키 질문 등록 기능 추가
fix: 티켓 상태 전이 오류 수정
docs: API 계약서 갱신
chore: Flyway migration 설정 추가
```

권장 type:

| type | 의미 |
|---|---|
| `feat` | 기능 |
| `fix` | 버그 |
| `docs` | 문서 |
| `chore` | 설정/빌드 |
| `refactor` | 리팩터링 |
| `test` | 테스트 |

## 7. Issue 규칙

개발 작업은 Issue를 먼저 만들고 시작한다.
Issue는 작업 범위, 담당자, 완료 기준을 팀이 같은 기준으로 이해하기 위한 단위다.

기본 원칙:

- skeleton 구현, 기능 구현, 버그 수정, 문서 정리는 작업 시작 전에 Issue를 만든다.
- 브랜치는 Issue를 기준으로 만든다.
- PR 본문에는 관련 Issue를 연결한다.
- 매일 진행상황은 Issue 댓글 또는 `docs/006-planning/daily-discussions/`에 남긴다.

### 7.1 Issue 단위

| 구분 | 기준 | 예시 |
|---|---|---|
| Weekly Issue | 한 주의 공통 목표와 담당자별 체크리스트 | `[WBS] Week 1 기반 개발 작업` |
| Feature Issue | 하나의 기능 또는 도메인 작업 | `[feat] Auth JWT 로그인 skeleton` |
| Bug Issue | 재현 가능한 오류 수정 | `[fix] 티켓 상태 변경 오류 수정` |
| Docs Issue | 문서 구조, API 계약, WBS 정리 | `[docs] API 계약서 갱신` |

매일 Daily Issue를 따로 만들지는 않는다.
일 단위 기록은 `daily-discussions/YYYY-MM-DD.md`에 남기고, GitHub Issue는 추적 단위로 사용한다.

### 7.2 Issue 작성 템플릿

```md
## 작업 목적
-

## 작업 범위
- [ ]
- [ ]
- [ ]

## 완료 기준
- [ ]

## 참고 문서
-
```

### 7.3 Issue 기반 작업 흐름

```text
Issue 생성
-> Issue 번호 기준으로 브랜치 생성
-> skeleton 또는 기능 구현
-> 로컬 테스트/API 수동 확인
-> PR 생성
-> PR 본문에 관련 Issue 연결
```

브랜치 예시:

```text
feat/auth-jwt-12
feat/ticket-skeleton-13
docs/week1-wbs-14
```

## 8. PR 규칙

문서 PR 제목은 `docs: 작업 요약` 형식으로 통일한다.

### 8.1 PR 크기

- 한 PR은 가능하면 한 기능 또는 한 흐름만 포함한다.
- DB migration, API 변경, 프론트 연동 변경은 PR 설명에 반드시 적는다.
- 큰 기능은 skeleton PR → 세부 구현 PR → 연동 PR로 나눈다.

### 8.2 PR 제목

```text
type: 작업 요약
```

예시:

```text
feat: Auth JWT 로그인 구현
feat: 워키 질문/답변 API 구현
docs: WBS 및 API 계약서 추가
```

### 8.3 PR 설명 템플릿

```md
## 작업 내용
- 

## 변경 API
- 

## DB 변경
- 

## 테스트
- [ ] 로컬 테스트 완료
- [ ] 관련 API 수동 확인

## 확인 필요
- 

## 관련 이슈
- close #
```

## 9. merge 규칙

| 대상 브랜치 | merge 방식 | 규칙 |
|---|---|---|
| `develop` | Squash merge 권장 | 기능 단위 이력을 깔끔하게 유지 |
| `release/*` | Merge commit 또는 PR merge | 안정화 이력 보존 |
| `main` | PR merge만 허용 | 직접 push 금지 |

merge 전 체크:

- 로컬에서 빌드 또는 테스트를 확인한다.
- API 변경이 있으면 `docs/004-api/api-contract.md`를 갱신한다.
- DB 변경이 있으면 migration과 `docs/005-database/db-migration-guide.md`를 확인한다.
- 프론트 영향이 있으면 황희수에게 공유한다.

## 10. 리뷰 규칙

| 변경 유형 | 리뷰 기준 |
|---|---|
| 문서 | 1명 확인 |
| 일반 API | 관련 프론트/백엔드 담당 1명 확인 |
| Auth/Security | 2명 확인 권장 |
| DB migration | 2명 확인 권장 |
| RAG/LLM/Embedding | 김진혁 + 관련 담당 확인 |
| 관리자/admin_logs | 김가영 + 관련 담당 확인 |

## 11. 충돌 줄이는 규칙

- 공통 파일(`build.gradle`, `application.yaml`, 공통 응답/예외)은 한 번에 여러 명이 수정하지 않는다.
- API path나 request/response가 바뀌면 `docs/004-api/api-contract.md`를 먼저 수정한다.
- 테이블/컬럼이 바뀌면 `docs/005-database/db-migration-guide.md`와 migration 파일을 같이 수정한다.
- enum/status 값은 프론트가 의존하므로 변경 즉시 황희수에게 공유한다.
- README.md는 발표/최종 산출물 정리 전까지 임의 수정하지 않는다.

## 12. 브랜치 작업 흐름

```text
통합 브랜치 최신화
-> Issue 확인 또는 생성
-> 기능 브랜치 생성
-> 작은 단위 구현
-> 로컬 테스트
-> 문서/API 변경 반영
-> PR 생성
-> 리뷰 반영
-> 통합 브랜치 merge
```

예시:

```bash
git switch chore/init-setting
git pull
git switch -c feat/ticket-transfer

# 작업 후
git add .
git commit -m "feat: 티켓 이관 기능 추가"
git push -u origin feat/ticket-transfer
```

## 13. 배포 전 브랜치 운영

배포 목표일은 2026-06-26이다.

| 날짜 | 운영 방식 |
|---|---|
| ~ 2026-06-19 | 기능 PR 적극 merge |
| 2026-06-22 ~ 2026-06-24 | 버그픽스와 통합 안정화 중심 |
| 2026-06-25 | 배포 후보 브랜치 고정 |
| 2026-06-26 | 최종 배포 |
| 2026-06-29 ~ 2026-07-02 | 발표용 hotfix만 허용 |

배포 후보 브랜치 예시:

```text
release/2026-06-26
```

## 14. hotfix 전략

발표 직전 또는 배포 후보 브랜치에서 시연을 막는 문제가 생기면 `hotfix/*` 브랜치를 사용한다.

```text
release/2026-06-26
  -> hotfix/demo-ticket-status
  -> release/2026-06-26
  -> main
  -> develop 역반영
```

규칙:

- hotfix는 시연 차단 버그만 허용한다.
- 기능 추가는 hotfix로 처리하지 않는다.
- hotfix 후 `develop`에도 반드시 반영한다.

## 14. 금지 사항

- 공유 브랜치에서 직접 큰 기능 개발 금지
- 리뷰 없이 DB migration 변경 merge 금지
- force push 금지
- 다른 사람이 작업 중인 파일을 임의로 대규모 리팩터링 금지
- README.md 임의 수정 금지
- 시연 직전 대규모 구조 변경 금지
