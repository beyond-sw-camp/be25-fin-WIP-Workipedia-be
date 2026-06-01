# Admin/Reward/ESG Domain Guide

> 문서 유형: Development Guide
> 상태: Draft
> 정본 위치: `docs/010-development/domain-guides/admin-reward-esg.md`
> 관련 문서: `docs/003-adr/005-role-permission-strategy.md`, `docs/003-adr/006-knowledge-conversion-strategy.md`, `docs/001-reference/project-proposal.md`, `docs/004-api/api-contract.md`
> 버전: v0.1
> 최종 수정: 2026-05-31

## 개발 목표

관리자 대시보드, 포인트/뱃지, ESG 지표를 MVP 시연 가능한 수준으로 구현한다.

## 먼저 볼 문서

- `docs/003-adr/005-role-permission-strategy.md`
- `docs/003-adr/006-knowledge-conversion-strategy.md`
- `docs/001-reference/project-proposal.md`
- `docs/004-api/api-contract.md`

## MVP 구현 범위

- 팀별 티켓 통계
- 공통 접수 큐 통계
- 자동 배정 성공률
- 지식화 전환 건수
- 예상 절감 시간 카드
- 포인트 적립
- 기본 뱃지 부여
- 관리자 작업 로그 기준 반영

## API/DB 영향

- `user_points`
- `point_history`
- `points_daily_limit`
- `esg_grade`
- `badges`
- `user_badges`
- `admin_logs`
- dashboard summary APIs
- ESG grade/score APIs

## 권한/보안 체크

- 대시보드는 개인 평가가 아니라 운영 지표 중심으로 구성한다.
- `SYSTEM_ADMIN`은 공통 접수 큐와 전체 운영 지표를 본다.
- `TEAM_ADMIN`은 자기 팀 지표와 지식화 승인 범위를 본다.
- 민감한 티켓 본문을 전체 대시보드에 직접 노출하지 않는다.

## 완료 기준

- 관리자 대시보드에서 핵심 카드가 조회된다.
- 포인트가 이벤트에 따라 적립된다.
- 기본 뱃지가 조건에 따라 부여된다.
- 지식화 전환 건수와 예상 절감 시간이 계산된다.

## 논의 필요 사항

- 포인트 지급 기준
- 뱃지 등급 기준
- ESG 절감 시간 산식
- 관리자 로그 기록 대상 액션
