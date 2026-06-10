# ADR 012 - Flash Chat Broker Strategy

> 문서 유형: ADR
> 상태: Accepted
> 정본 위치: `docs/adr/012-flash-chat-broker-strategy.md`
> 관련 문서: `docs/adr/011-flash-chat-strategy.md`
> 버전: v0.1
> 최종 수정: 2026-06-07

## Context

Flash Chat은 전사 단일 공개 채널에서 실시간 메시지를 브로드캐스트한다. 이를 위해 세 가지 선택이 필요했다.

1. 실시간 통신 프로토콜: 순수 WebSocket vs STOMP
2. 브로커 위치: Simple Broker(인메모리) vs 외부 브로커(RabbitMQ, Kafka 등)
3. 외부 브로커가 필요하다면: RabbitMQ vs Kafka

## Decision

**STOMP + Spring Simple Broker**로 구현한다. 스케일아웃 필요 시 RabbitMQ로 전환한다.

## Rationale

### 1. 순수 WebSocket 대신 STOMP를 선택한 이유

순수 WebSocket은 raw 데이터만 전달하므로 서버가 메시지 종류와 목적지를 직접 파싱해야 한다. 연결된 클라이언트 목록을 `ConcurrentHashMap`으로 직접 관리해야 하고, 브로드캐스트 로직도 직접 구현해야 한다.

STOMP는 메시지에 `destination`이 포함된 표준 형식을 제공한다. Spring이 `destination`을 보고 `@MessageMapping` 컨트롤러로 자동 라우팅하며, 브로드캐스트는 아래 한 줄로 끝난다.

```java
messagingTemplate.convertAndSend("/topic/flash-chat", message);
```

`spring-boot-starter-websocket`에 STOMP가 내장돼 있어 추가 의존성도 없다.

### 2. 외부 브로커 대신 Simple Broker를 선택한 이유

외부 브로커(RabbitMQ 등)는 Spring 서버가 여러 대일 때 필요하다. 서버가 2대 이상이면 각 서버의 구독 목록이 공유되지 않아 메시지가 일부 클라이언트에게 전달되지 않는다.

```
# 외부 브로커 없이 서버 2대인 경우
Pod 1 (클라이언트 A, C 접속) ← 메시지 수신 ✅
Pod 2 (클라이언트 B, D 접속) ← 메시지 수신 ❌
```

Workipedia MVP는 서버 1대로 운영하므로 Simple Broker로 충분하다. 외부 브로커는 별도 프로세스(Redis, MariaDB처럼)로 띄워야 하므로 인프라 복잡도가 올라간다.

### 3. 스케일아웃 시 RabbitMQ를 선택할 이유 (Kafka 대신)

Kubernetes 환경에서 Auto Scaling으로 Pod가 여러 개 뜨면 외부 브로커가 필요해진다.

이때 Kafka보다 RabbitMQ가 적합한 이유:
- Flash Chat은 실시간 브로드캐스트가 목적이며, 메시지를 디스크에 쌓아놓을 필요가 없다. Kafka는 로그 저장/재처리에 강점이 있으나 이 사용 사례에는 과도하다.
- RabbitMQ는 STOMP 프로토콜을 기본 지원한다. Simple Broker에서 RabbitMQ로의 전환은 `enableSimpleBroker`를 `enableStompBrokerRelay`로 교체하는 것으로 끝난다.

```java
// 현재 (Simple Broker)
registry.enableSimpleBroker("/topic");

// 스케일아웃 시 (RabbitMQ)
registry.enableStompBrokerRelay("/topic")
        .setRelayHost("rabbitmq")
        .setRelayPort(61613);
```

Kafka는 STOMP를 직접 지원하지 않아 별도 어댑터가 필요하다.

## Consequences

- MVP에서는 인프라 추가 없이 Simple Broker로 운영한다.
- Kubernetes 스케일아웃 시 `WebSocketConfig.java` 한 곳만 수정하면 RabbitMQ로 전환 가능하다.
- RabbitMQ 도입 시 Docker Compose / Kubernetes에 RabbitMQ 컨테이너가 추가된다.
- Flash Chat 메시지는 Redis TTL로 관리하므로 브로커 전환 시에도 메시지 저장 구조는 변경 없다.
