# Worki/Notification Domain Guide

> 문서 유형: Development Guide
> 상태: Draft
> 정본 위치: `docs/010-development/domain-guides/worki-notification.md`
> 관련 문서: `docs/003-adr/006-knowledge-conversion-strategy.md`, `docs/003-adr/007-notification-strategy.md`, `docs/001-reference/service-flow.md`, `docs/004-api/api-contract.md`
> 버전: v0.1
> 최종 수정: 2026-05-31

## 개발 목표

워키 질문/답변/채택 흐름과 알림 저장/조회 흐름을 구현한다.

## 먼저 볼 문서

- `docs/003-adr/006-knowledge-conversion-strategy.md`
- `docs/003-adr/007-notification-strategy.md`
- `docs/001-reference/service-flow.md`
- `docs/004-api/api-contract.md`

## MVP 구현 범위

- 워키 질문 등록
- 워키 답변 등록
- 답변 채택
- FAQ 조회
- 티켓 처리 완료 후 지식화된 워키 반영
- 알림 생성
- 알림 목록 조회
- 알림 읽음 처리

## API/DB 영향

- `worki_questions`
- `worki_answers`
- `faq`
- `notifications`
- notification target URL/type
- answer accepted flag

## 권한/보안 체크

- 질문/답변 삭제는 관리자 정책과 충돌하지 않아야 한다.
- 채택 이후 추가 답변 가능 여부를 정책에 맞춘다.
- 알림은 대상 사용자 또는 대상 role에게만 노출한다.

## 완료 기준

- 워키 질문과 답변을 등록할 수 있다.
- 질문자가 답변을 채택할 수 있다.
- 답변/채택/티켓 상태 변경 시 알림이 생성된다.
- 사용자는 본인 알림을 조회하고 읽음 처리할 수 있다.

## 논의 필요 사항

- 채택 이후 추가 답변 차단 여부
- 알림 삭제 허용 여부
- 팀 알림 대상 범위
- FAQ와 워키의 경계
