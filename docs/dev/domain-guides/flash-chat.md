# Flash Chat Domain Guide

> 문서 유형: Development Guide
> 상태: Draft
> 정본 위치: `docs/dev/domain-guides/flash-chat.md`
> 관련 문서: `docs/api/api-contract.md`
> 버전: v0.1
> 최종 수정: 2026-06-08

## 개발 목표

실시간 사내 닉네임 기반 공개 채팅 기능. 메시지는 Redis에 TTL로 저장되고 만료 시 자동 삭제된다. 관리자는 정책(TTL, 쿨다운, 금지어)을 설정하고 메시지를 강제 삭제할 수 있다.

## 먼저 볼 문서

- `docs/api/api-contract.md`
- `docs/dev/db-migration-guide.md`

## MVP 구현 범위

- STOMP 실시간 메시지 전송/수신 (`/app/flash-chat/send` → `/topic/flash-chat` 브로드캐스트)
- Redis TTL 저장 (Hash + Sorted Set)
- 활성 메시지 목록 REST 조회
- 관리자 정책 관리 (TTL, 쿨다운, 금지어)
- 관리자 메시지 강제 삭제
- AdminLog 기록

## 아키텍처

### Redis 키 구조

| 키 | 타입 | 설명 |
|---|---|---|
| `flash-chat:msg:{uuid}` | Hash | 메시지 상세 (userId, nickname, content, replyToId, createdAt, expiresAt) |
| `flash-chat:messages` | Sorted Set | 메시지 목록 (score = epoch millis) |
| `flash-chat:cooldown:{userId}` | String TTL | 쿨다운 제어 |

### WebSocket 엔드포인트

| 엔드포인트 | 용도 |
|---|---|
| `/ws/flash-chat` | 프론트엔드 (SockJS 클라이언트) |
| `/ws/flash-chat-native` | 테스트용 (native WebSocket 연결 위 STOMP) |

### STOMP

- Subscribe: `/topic/flash-chat`
- Send: `/app/flash-chat/send`
- Broadcast type: `MESSAGE`, `DELETE`

## API

| Method | URL | 설명 |
|---|---|---|
| GET | `/api/v1/flash-chat/messages` | 활성 메시지 목록 조회 |
| GET | `/api/v1/admin/flash-chat/policy` | 정책 조회 |
| PATCH | `/api/v1/admin/flash-chat/policy` | 정책 수정 |
| DELETE | `/api/v1/admin/flash-chat/messages/{messageId}` | 메시지 강제 삭제 |

## DB

- `flash_chat_policy` — 단일 행 정책 테이블 (id=1 고정)
- `admin_logs` — 관리자 액션 로그 (`FLASH_CHAT_MESSAGE_DELETE`, `FLASH_CHAT_CONFIG_UPDATE`)

## 권한/보안

- 현재 Auth skeleton 상태: `SKELETON_USER_ID = 1L`, nickname = `"노잇0001"`
- Auth 구현 완료 후 JWT에서 userId/nickname 추출로 교체 필요
- 관리자 엔드포인트(`/admin/**`)는 추후 `SYSTEM_ADMIN` 권한 체크 추가 필요

## 로컬 테스트 방법

### REST 엔드포인트 (APIdog)

1. `GET http://localhost:8080/api/v1/flash-chat/messages` — 활성 메시지 조회
2. `GET http://localhost:8080/api/v1/admin/flash-chat/policy` — 정책 조회
3. `PATCH http://localhost:8080/api/v1/admin/flash-chat/policy` — 정책 수정
4. `DELETE http://localhost:8080/api/v1/admin/flash-chat/messages/{messageId}` — 메시지 삭제

### WebSocket STOMP (터미널 스크립트)

APIdog/Postman은 STOMP 프레임 미지원. 아래 스크립트로 테스트:

```bash
node test-stomp.cjs
```

> `test-stomp.cjs` 위치: 프로젝트 루트 (`Workipedia/test-stomp.cjs`)

스크립트 실행 후 `GET /api/v1/flash-chat/messages` 호출해서 메시지 저장 확인.

### 프론트엔드 연동 (Vue)

```javascript
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/ws/flash-chat'),
  onConnect: () => {
    client.subscribe('/topic/flash-chat', (msg) => {
      console.log(JSON.parse(msg.body))
    })
  }
})
client.activate()

// 메시지 전송
client.publish({
  destination: '/app/flash-chat/send',
  body: JSON.stringify({ content: '안녕하세요', replyToId: null })
})
```

## 완료 기준

- STOMP 메시지 전송 시 `/topic/flash-chat`으로 브로드캐스트된다.
- 전송된 메시지가 Redis에 TTL과 함께 저장된다.
- `GET /flash-chat/messages`로 활성 메시지 목록을 조회할 수 있다.
- TTL 만료 후 메시지가 자동 삭제된다.
- 관리자가 정책(TTL, 쿨다운, 금지어)을 수정할 수 있다.
- 관리자가 특정 메시지를 강제 삭제하면 AdminLog가 기록된다.

## 논의 필요 사항

- Auth 구현 후 skeleton 코드 교체 시점
- 금지어 필터 고도화 여부 (정규식, 초성 우회 등)
- 메시지 신고 기능 추가 여부
