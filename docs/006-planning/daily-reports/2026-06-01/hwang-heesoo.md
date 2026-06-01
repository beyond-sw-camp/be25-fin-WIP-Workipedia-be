# Daily Report — 황희수 2026-06-01

> 문서 유형: Daily Report
> 상태: Draft
> 정본 위치: `docs/006-planning/daily-reports/2026-06-01/hwang-heesoo.md`
> 관련 문서: `docs/006-planning/daily-plans/2026-06-01.md`, `docs/006-planning/weekly-wbs/2026-06-01-week1.md`, `docs/006-planning/member-wbs/hwang-heesoo.md`
> 버전: v0.1
> 최종 수정: 2026-06-01

## 완료

- 팀 대시보드 페이지 기획 확정 (팀 관리자 대시보드 + 내 티켓 페이지 통합)
  - USER/TEAM_ADMIN 공통 페이지, 지식화 승인은 TEAM_ADMIN에게만 표시
  - 상단 요약 카드: 총 티켓 / 내 티켓 / 처리 완료
  - 메인 섹션: 티켓 (부서 티켓 / 내 티켓 / 처리 완료 필터 탭) + 지식화 승인
 
- 전체 관리자 대시보드 기획 확정
  - 메인 섹션: 공통 접수 큐 고정 노출 (목록 조회 + 담당 부서 수동 지정)
  - 하단 탭: 티켓 통계 / 매뉴얼 관리 / 포인트 사용 / 부서 관리 / 사용자 관리
  - 부서 관리 탭 평균 인원 항목 제거

- 마이페이지 기획 확정
  - 업적 섹션 제거
  - 환경에 미친 영향, 북극 환경 회복도 → 리더보드 페이지로 이동

## 미완료

- Figma Make 실제 반영 및 정합성 확인
- 프론트 프로젝트 skeleton 구조 잡기
- 라우팅 설계
- 공통 레이아웃 구성
- 로그인 / 챗봇 mock 화면 시작
- 공통 응답 최종 필드명 논의 참여 (개발 시작 전 결정 필요)

## 다음 근무일 논의

- 내 티켓 페이지에서 USER와 TEAM_ADMIN의 role 분기 방식 (김진혁과 확인 필요)
  - 부서 티켓에서 이관하기 버튼 TEAM_ADMIN에게만 노출하는 구조
- TEAM_ADMIN 팀원 배정 기능 제거 내용 문서 반영 필요 (ticket.md, ADR 005 수정 — 김진혁과 확인 후)
- 티켓 상세 페이지 구성 논의 필요

## API/DB/화면 영향

- 팀 대시보드: 부서 배정 티켓 목록 API, 내 담당 티켓 목록 API, 처리 완료 티켓 API 필요
- 전체 관리자 대시보드: 공통 접수 큐 목록 API, 담당 부서 지정 API 필요
- 지식화 승인: 지식화 후보 목록 API, 승인/반려 API 필요
- 티켓 통계 / 자동 배정 성공률: summary API 별도 필요

## 관련 링크

- `docs/006-planning/daily-plans/2026-06-01.md`
- `docs/003-adr/004-ticket-routing-strategy.md`
- `docs/003-adr/005-role-permission-strategy.md`
- `docs/003-adr/006-knowledge-conversion-strategy.md`
