# Frontend Integration Guide

> 문서 유형: Development Guide
> 상태: Draft
> 정본 위치: `docs/dev/domain-guides/frontend-integration.md`
> 관련 문서: `docs/api/api-contract.md`, `docs/reference/service-flow.md`, `docs/planning/member-wbs/hwang-heesoo.md`
> 버전: v0.1
> 최종 수정: 2026-06-04

## 개발 목표

Figma Make로 만든 화면을 현재 서비스 흐름과 API 계약에 맞게 조정하고, 백엔드 API 연동 기준을 맞춘다.

프론트 기본 골격은 황희수가 담당하고, 민정기는 BE 작업 완료 후 챗봇 화면, 모바일 대응, CDN 챗봇 컴포넌트/스크립트 주입 범위에 합류한다.

## 먼저 볼 문서

- `docs/reference/service-flow.md`
- `docs/api/api-contract.md`
- `docs/planning/member-wbs/hwang-heesoo.md`
- `docs/dev/development-guide.md`

## MVP 구현 범위

- 로그인 화면
- 질문/챗봇 화면 (민정기 합류)
- 요청 티켓 생성 화면
- 내 티켓 상태 화면
- 워키 질문/답변 화면
- 팀 관리자 티켓 큐
- 공통 접수 큐
- 관리자 대시보드
- 알림 패널
- Flash Chat (민정기 합류)
- 티켓 사진 첨부/카메라 캡처 (민정기 합류)
- 모바일 핵심 흐름 반응형 대응 (민정기 합류)
- CDN 챗봇 컴포넌트 빌드/스크립트 주입 가이드 (민정기 합류)

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
- 티켓 발행 화면에서 중요도와 사진 첨부가 동작한다.
- 모바일 화면에서 로그인, 챗봇, 티켓 발행, 내 티켓 확인 흐름이 깨지지 않는다.
- Flash Chat에서 메시지 전송, 답장, 반응이 동작한다.

## 논의 필요 사항

- Figma Make 화면 중 현재 기획과 다른 부분
- 질문/요청 진입 UI
- 티켓 상태 표시 방식
- 공식 답변 강조 방식
- 관리자 대시보드 카드 우선순위
- Capacitor 래핑을 실제 MVP 범위에 포함할지 여부
- CDN 챗봇 컴포넌트 허용 도메인 정책
