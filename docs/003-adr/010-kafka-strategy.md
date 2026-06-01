# ADR 010 - Kafka Strategy

> 문서 유형: ADR
> 상태: Draft
> 정본 위치: `docs/003-adr/010-kafka-strategy.md`
> 관련 문서: `docs/003-adr/007-notification-strategy.md`, `docs/001-reference/trd.md`
> 버전: v0.1
> 최종 수정: 2026-06-01

## Context

Workipedia에서는 답변 등록, 티켓 상태 변경, 채택, 지식화 반영 등 다양한 도메인 이벤트가 발생한다.
ADR 007에서 MVP 알림은 DB 저장 + 조회 API 기반으로 시작하기로 했으나, 도메인 간 이벤트 전파를 위한 메시지 브로커가 필요하다.

## Decision

이벤트 기반 도메인 간 통신에 **Kafka**를 사용한다.

주요 사용 목적:
- 도메인 이벤트 발행 (답변 등록, 채택, 티켓 상태 변경 등) → 알림 생성
- 포인트/ESG 점수/등급 이벤트 발행 → 보상 처리
- Elasticsearch 인덱싱 이벤트 (워키/매뉴얼 변경 시 chunk 재인덱싱)
- 배치 작업 트리거

## Consequences

- 도메인 간 직접 의존 없이 이벤트 기반으로 느슨하게 연결할 수 있다.
- docker-compose에 Kafka(+ Zookeeper 또는 KRaft) 컨테이너가 추가된다.
- 로컬 환경 요구사항이 늘어난다.
- 장애 시 이벤트 유실 가능성이 있으므로 idempotent consumer 설계가 필요하다.

## Open Questions

- Kafka 버전 및 KRaft 모드 사용 여부 확정 필요 (ZooKeeper 없이 KRaft 권장).
- 토픽 네이밍 규칙 확정 필요.
- 이벤트 스키마(Avro vs JSON) 확정 필요.
- MVP에서 Kafka 없이 동작 가능한 fallback 전략 필요 여부 검토.
