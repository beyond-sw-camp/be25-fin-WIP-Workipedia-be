# Reference Document Guide

> 목적: 팀원이 프로젝트의 제품/기술/원칙 문서를 같은 기준으로 참고하도록 정리한다.
> 루트 `README.md`는 최종 산출물 정리용으로 유지하고, 작업 중 참고 문서는 `docs/` 아래에서 관리한다.

## 문서 목록

| 문서 | 파일 | 역할 |
|---|---|---|
| Constitution | `constitution.md` | 프로젝트 불변 원칙과 의사결정 기준 |
| PRD | `prd.md` | 제품 요구사항, 사용자, 기능 범위, 성공 지표 |
| TRD | `trd.md` | 기술 구조, 데이터 모델, API/보안/운영 요구사항 |
| Project Structure | `../architecture/project-structure.md` | 모듈러 모놀리스 구조와 도메인 경계 |
| WBS | `../planning/wbs.md` | 팀 역할, 일정, 주차별 작업 |
| Member WBS | `../planning/member-wbs/` | 개인별 작업 범위 |
| API Contract | `../api/api-contract.md` | 프론트/백엔드 요청·응답 계약 |
| DB Migration Guide | `../database/db-migration-guide.md` | Flyway migration 규칙과 테이블 생성 순서 |
| Harness Guide | `../quality/harness-engineering.md` | 출처, 개인정보, 권한, 워크플로우 검증 기준 |
| Midterm Guide | `../presentation/midterm-presentation-guide.md` | 중간 발표 메시지와 시연 구성 |
| ADRs | `../adr/` | DB/Auth/RAG 등 주요 기술 결정 기록 |

## 읽는 순서

1. `constitution.md`
2. `prd.md`
3. `trd.md`
4. `../architecture/project-structure.md`
5. `../planning/wbs.md`
6. `../api/api-contract.md`
7. `../database/db-migration-guide.md`
8. `../adr/0001-database-choice.md`
9. `../adr/0002-rag-strategy.md`
10. `../adr/0003-auth-strategy.md`
11. `../quality/harness-engineering.md`
12. `../presentation/midterm-presentation-guide.md`

## 관리 규칙

- 정책이나 원칙 변경은 `constitution.md`와 충돌하지 않아야 한다.
- 제품 범위 변경은 `prd.md`에 먼저 반영한 뒤 구현한다.
- 기술 구조, 테이블, API 변경은 `trd.md` 또는 별도 기술 문서에 반영한다.
- 루트 `README.md`는 발표/최종 산출물 정리 시점에만 갱신한다.
- 작업 중 문서는 `docs/` 아래에 만들고, 루트 `README.md`에 임의 링크를 추가하지 않는다.

## 공통 문서 양식

새 문서는 아래 메타데이터를 상단에 둔다.

```md
# 문서 제목

> 문서 유형: PRD / TRD / WBS / ADR / Guide 등
> 상태: Draft / Review / Approved
> 정본 위치: `docs/...`
> 관련 문서: `docs/...`
> 버전: v0.1
> 최종 수정: YYYY-MM-DD
```
