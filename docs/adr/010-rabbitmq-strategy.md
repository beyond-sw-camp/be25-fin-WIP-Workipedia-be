# ADR 010 - RabbitMQ Strategy

> 문서 유형: ADR
> 상태: Accepted
> 정본 위치: `docs/adr/010-rabbitmq-strategy.md`
> 관련 문서: `docs/adr/007-notification-strategy.md`, `docs/adr/012-flash-chat-broker-strategy.md`, `docs/reference/trd.md`
> 버전: v0.2
> 최종 수정: 2026-06-11

## Context

Workipedia에서는 답변 등록, 티켓 상태 변경, 채택, 포인트 지급, ESG 반영 등 다양한 비동기 도메인 이벤트가 발생한다.
이벤트 스트리밍과 장기 재처리보다 작업 전달, ACK, 재시도와 실패 큐 관리가 중요하므로 Kafka보다 RabbitMQ가 현재 요구사항에 적합하다.

## Decision

비동기 도메인 이벤트와 작업 큐에 **RabbitMQ**를 사용한다.

주요 사용 목적:
- 도메인 이벤트 발행 (답변 등록, 채택, 티켓 상태 변경 등) → 알림 생성
- 포인트/ESG 점수/등급 이벤트 발행 → 보상 처리
- 실패 메시지 재시도와 Dead Letter Queue 관리

사용하지 않는 범위:
- AI 지식 동기화는 `ai_sync_jobs`와 Spring `@Scheduled` 워커로 처리한다.
- Flash Chat은 단일 서버 MVP에서 Spring Simple Broker를 사용하며, 서버 스케일아웃 시 RabbitMQ STOMP Broker Relay를 사용한다.
- RabbitMQ를 데이터의 source of truth로 사용하지 않는다.

## Consequences

- 도메인 간 직접 의존 없이 이벤트 기반으로 느슨하게 연결할 수 있다.
- Docker Compose와 운영 인프라에 RabbitMQ 컨테이너가 추가된다.
- 로컬 환경 요구사항이 늘어난다.
- publisher confirm, consumer ACK, DLQ와 멱등 consumer 설계가 필요하다.
- 이벤트의 장기 보관·재생이 필요한 요구가 생기면 Kafka 도입을 별도로 검토한다.

## Open Questions

- exchange, queue, routing key 네이밍 규칙
- retry 횟수와 backoff, DLQ 재처리 정책
- 메시지 JSON schema와 버전 관리 방식
- RabbitMQ 장애 시 동기 fallback을 허용할 이벤트 범위
