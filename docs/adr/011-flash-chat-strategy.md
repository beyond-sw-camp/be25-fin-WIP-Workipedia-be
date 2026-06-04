# ADR 011 - Flash Chat Strategy

> 문서 유형: ADR
> 상태: Draft
> 정본 위치: `docs/adr/011-flash-chat-strategy.md`
> 관련 문서: `docs/reference/prd.md`, `docs/reference/trd.md`, `docs/api/api-contract.md`, `docs/planning/member-wbs/min-jungki.md`
> 버전: v0.1
> 최종 수정: 2026-06-04

## Context

Workipedia는 질문(챗봇/워키)과 요청(티켓)을 중심으로 동작한다. 다만 모든 질문을 공식 기록으로 남기면 사용자가 가벼운 질문을 하기 부담스럽고, 동료에게 빠르게 확인하면 되는 내용까지 워키/티켓으로 흘러갈 수 있다.

Flash Chat은 전사 단일 공개 채널에서 가벼운 질문을 빠르게 주고받는 임시 채널이다. 공식 지식으로 보존할 내용은 워키/티켓/지식화 흐름으로 전환하고, Flash Chat 메시지는 영구 보존하지 않는다.

## Decision

Flash Chat은 **Spring WebSocket + STOMP + Redis TTL**로 구현한다.

| 항목 | 결정 |
|---|---|
| 채널 | 전사 단일 공개 채널 |
| 실시간 통신 | Spring WebSocket + STOMP |
| 구독 topic | `/topic/flash-chat` |
| 메시지 전송 | `/app/flash-chat/send` |
| 반응 전송 | `/app/flash-chat/react` |
| 활성 메시지 조회 | `GET /flash-chat/messages` |
| 저장소 | Redis |
| 기본 TTL | 600초 |
| 영구 저장 | 하지 않음 |

SYSTEM_ADMIN은 메시지 TTL, 전송 쿨다운, 금지어, 강제 삭제를 관리한다. 강제 삭제와 설정 변경은 `admin_logs`에 기록한다.

## Consequences

- 가벼운 질문을 공식 티켓/워키와 분리해 사용자 진입 부담을 낮춘다.
- Redis TTL을 사용해 임시성을 명확히 보장한다.
- 메시지 영구 저장을 하지 않으므로 검색/감사 대상 지식으로 쓰지 않는다.
- 운영 정책은 관리자 설정으로 조정 가능해야 한다.
- WebSocket 연결 실패 시 MVP에서는 활성 메시지 조회로 초기 상태만 복구하고, 완전한 fallback은 후순위로 둔다.

## Open Questions

- 활성 메시지 최대 보존 개수
- Flash Chat 알림을 이슬이 담당 알림 시스템과 얼마나 강하게 연결할지
- 부적절한 메시지 신고 기능을 MVP에 포함할지
