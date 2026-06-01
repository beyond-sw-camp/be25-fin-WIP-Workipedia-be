# Daily Report - 김가영 2026-06-01

> 문서 유형: Daily Report
> 상태: Draft
> 원본 위치: `docs/006-planning/daily-reports/2026-06-01/kim-gayeong.md`
> 관련 문서: `docs/006-planning/daily-plans/2026-06-01.md`, `docs/006-planning/weekly-wbs/2026-06-01-week1.md`, `docs/006-planning/member-wbs/kim-gayeong.md`, `docs/998-handoffs/kim-gayeong-2026-06-01.md`
> 버전: v0.2
> 최종 수정: 2026-06-01

## 완료

- 주말 사이 변경된 관리자/포인트/ESG/뱃지 범위와 관련 문서를 확인했다.
- 관리자 대시보드 skeleton 범위를 검토하고 TEAM_ADMIN/SYSTEM_ADMIN 공통 컨트롤러 사용 방향을 확인했다.
- 티켓 처리 흐름을 담당자 사전 배정 방식에서 부서 티켓 큐 방식으로 조정했다.
- 최초 공식 답변 등록자를 티켓 처리자로 기록하는 방향으로 정리했다.
- 처리 완료 티켓은 TEAM_ADMIN 대시보드에서 지식화 검토 대상으로 노출하는 방향으로 정리했다.
- 관리자 대시보드 응답에서 예상 절감 시간 지표를 제외하고, 해당 지표는 ESG 지표 또는 마이페이지 성격으로 분리하는 방향을 반영했다.
- 포인트 응답에서 포인트 랭킹 개념을 제외하고, 순위는 ESG 기준에서만 사용하는 방향을 확인했다.
- 뱃지 API 더미 응답을 제거하고, repository 연동 전까지 빈 목록을 반환하는 방향으로 정리했다.
- `origin/dev` 최신 변경사항을 기능 브랜치에 반영하고 충돌을 해결했다.
- DB migration 기준으로 `badges`, `user_badges` 테이블 제거 및 `esg_grade` 테이블 유지 상태를 확인했다.
- `./gradlew.bat test` 실행 결과 빌드와 테스트가 통과했다.

## 미완료

- 관리자/포인트/ESG/뱃지 API는 현재 skeleton 중심이며 실제 repository 조회 로직은 추후 구현이 필요하다.
- 뱃지 도메인을 완전히 제거할지, API만 보류할지 최종 결정이 필요하다.
- 라우팅 신뢰도 점수 계산식은 정책 초안만 정리되었고 실제 계산 로직 구현은 미완료 상태다.
- 지식화 검토 대상의 상세 상태값과 승인/반려 이후 흐름은 추가 합의가 필요하다.

## 다음 근무일 논의

- 티켓 이관 요청 권한을 TEAM_ADMIN만 가질지, 부서원 누구나 요청할 수 있게 할지 결정이 필요하다.
- 공식 답변 등록 시 티켓을 즉시 `COMPLETED`로 변경할지, 별도 검수 상태를 둘지 확인이 필요하다.
- 처리 완료된 모든 티켓을 지식화 검토 대상으로 볼지, 제외 조건을 둘지 논의가 필요하다.
- 라우팅 신뢰도 점수 기준값을 문서화하고 향후 재조정 기준을 확정해야 한다.
- 뱃지를 제거하는 방향이면 badge 패키지와 API 계약도 함께 정리해야 한다.

## API/DB/화면 영향

- API: 관리자 팀 큐, 공통 큐, 지식화 검토 목록, 포인트 요약/이력, ESG 지표 API skeleton에 영향이 있다.
- API: 포인트 랭킹 API는 현재 담당 범위에서 제외하고 ESG 순위만 유지하는 방향이다.
- DB: `tickets.completed_by`로 최초 공식 답변 등록자를 처리자로 기록하는 방향이다.
- DB: 최신 migration 기준 `esg_grade`는 유지되고 `badges`, `user_badges`는 제거된 상태다.
- 화면: 부서 티켓함에서 부서원이 티켓을 처리하고, 팀장은 처리 완료 티켓을 지식화 검토 화면에서 확인하는 흐름이 필요하다.
- 화면: 예상 절감 시간은 관리자 대시보드가 아니라 ESG 지표 또는 마이페이지에서 보여주는 방향이다.

## 관련 링크

- `src/main/java/com/wip/workipedia/admin/`
- `src/main/java/com/wip/workipedia/point/`
- `src/main/java/com/wip/workipedia/esg/`
- `src/main/java/com/wip/workipedia/badge/`
- `src/main/resources/db/migration/V1__create_initial_schema.sql`
- `docs/004-api/api-contract.md`
- `docs/006-planning/member-wbs/kim-gayeong.md`
