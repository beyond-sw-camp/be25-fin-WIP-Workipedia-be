# Frontend Integration Guide

> 문서 유형: Development Guide
> 상태: Draft
> 정본 위치: `docs/010-development/domain-guides/frontend-integration.md`
> 관련 문서: `docs/004-api/api-contract.md`, `docs/001-reference/service-flow.md`, `docs/006-planning/member-wbs/hwang-heesoo.md`
> 버전: v0.1
> 최종 수정: 2026-05-31

## 개발 목표

Figma Make로 만든 화면을 현재 서비스 흐름과 API 계약에 맞게 조정하고, 백엔드 API 연동 기준을 맞춘다.

## 먼저 볼 문서

- `docs/001-reference/service-flow.md`
- `docs/004-api/api-contract.md`
- `docs/006-planning/member-wbs/hwang-heesoo.md`
- `docs/010-development/domain-guides/overview.md`

## MVP 구현 범위

- 로그인 화면
- 질문/챗봇 화면
- 요청 티켓 생성 화면
- 내 티켓 상태 화면
- 워키 질문/답변 화면
- 팀 관리자 티켓 큐
- 공통 접수 큐
- 관리자 대시보드
- 알림 패널

## API/DB 영향

프론트는 DB를 직접 다루지 않는다.
다만 화면 변경이 API request/response 변경을 요구하면 `api-contract.md`에 먼저 반영한다.

## 권한/보안 체크

- role에 따라 메뉴를 다르게 노출한다.
- `USER`, `TEAM_ADMIN`, `SYSTEM_ADMIN` 화면을 분리한다.
- access token 저장 위치는 Auth 결정에 맞춘다.
- 출처 없는 챗봇 답변 UI를 만들지 않는다.

## 완료 기준

- 핵심 시나리오 5개를 화면에서 따라갈 수 있다.
- API mock 또는 실제 API로 화면 상태가 변한다.
- 질문과 요청 진입이 명확히 분리된다.
- 챗봇 답변에는 매뉴얼/워키 출처가 보인다.

## 논의 필요 사항

- Figma Make 화면 중 현재 기획과 다른 부분
- 질문/요청 진입 UI
- 티켓 상태 표시 방식
- 공식 답변 강조 방식
- 관리자 대시보드 카드 우선순위
