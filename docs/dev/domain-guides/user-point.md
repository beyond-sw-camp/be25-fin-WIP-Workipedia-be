# User Point Domain Guide

> 문서 유형: Development Guide
> 상태: Draft
> 원본 위치: `docs/dev/domain-guides/user-point.md`
> 관련 문서: `docs/api/api-contract.md`, `docs/reference/prd.md`, `src/main/resources/db/migration/V1__create_initial_schema.sql`
> 버전: v0.1
> 최종 수정: 2026-06-10

## 개발 목표

사용자가 본인의 현재 보유 포인트와 포인트 변동 내역을 조회할 수 있게 한다. 포인트 적립은 사용자가 직접 호출하는 API로 제공하지 않고, Worki/티켓/지식화 등 다른 도메인 이벤트에서 시스템 내부 서비스가 자동으로 처리한다.

## MVP 구현 범위

- 현재 로그인한 사용자의 보유 포인트 조회
- 현재 로그인한 사용자의 포인트 내역 페이지 조회
- 포인트 내역 조회 시 `ALL`, `EARN`, `SPEND` 타입 필터 지원
- 시스템 내부 포인트 적립 메서드 제공
- 포인트 잔액 변경과 포인트 내역 저장을 하나의 트랜잭션으로 처리
- 존재하지 않는 사용자 포인트 row 조회 시 `currentPoint = 0`으로 응답

## API 명세

사용자에게 노출되는 포인트 API는 모두 Access Token 인증이 필요하며, 대상 사용자는 `@AuthenticationPrincipal`의 현재 사용자 ID로 결정한다.

아래 표는 요청사항에 적힌 외부 API 경로를 기준으로 작성한다. 현재 프로젝트의 기존 API 계약은 Base URL을 `/api/v1`로 두고 있으므로, 실제 Controller 구현 시에는 최종 노출 경로를 `/api/users/me/...`로 둘지 `/api/v1/users/me/...`로 둘지 프론트와 먼저 맞춘다.

| Method | Path                         | 설명 | 인증 |
|---|------------------------------|---|---|
| GET | `/api/v1/me/points`          | 현재 보유 포인트 조회 | Access Token |
| GET | `/api/v1/me/point-histories` | 포인트 변동 내역 조회 | Access Token |

## 1. 현재 보유 포인트 조회

```http
GET /api/v1/me/points
```

Response:

```json
{
  "userId": 1,
  "currentPoint": 1200
}
```

처리 규칙:

- 인증된 사용자 ID로 `user_points`를 조회한다.
- `deleted_at IS NULL`인 row만 유효한 포인트로 본다.
- row가 없으면 신규 사용자의 초기 상태로 간주하고 `currentPoint = 0`을 응답한다.
- 다른 사용자의 포인트를 조회하는 파라미터는 받지 않는다.

## 2. 포인트 내역 조회

```http
GET /api/v1/me/point-histories?type=ALL&page=0&size=20
GET /api/v1/me/point-histories?type=EARN&page=0&size=20
GET /api/v1/me/point-histories?type=SPEND&page=0&size=20
```

Query Parameters:

| 이름 | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| `type` | enum | 아니오 | `ALL` | `ALL`, `EARN`, `SPEND` |
| `page` | number | 아니오 | `0` | Spring Pageable 기준 0-base 페이지 번호 |
| `size` | number | 아니오 | `20` | 페이지 크기 |

Response:

```json
{
  "content": [
    {
      "pointHistoryId": 10,
      "pointAmount": 100,
      "type": "EARN",
      "reasonType": "WORKI_ANSWER_ACCEPTED",
      "relatedType": "WORKI_ANSWER",
      "relatedId": 25,
      "createdAt": "2026-06-10T13:30:00"
    }
  ],
  "pageInfo": {
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

처리 규칙:

- 인증된 사용자 ID의 `point_history`만 조회한다.
- `deleted_at IS NULL`인 내역만 조회한다.
- 최신 내역이 먼저 오도록 `created_at DESC` 정렬을 기본으로 한다.
- `type=ALL`이면 적립/사용 내역을 모두 반환한다.
- `type=EARN`이면 `point_amount > 0` 내역만 반환한다.
- `type=SPEND`이면 `point_amount < 0` 내역만 반환한다.
- 잘못된 `type` 값은 `400 bad_request`로 처리한다.

## 3. 시스템 자동 포인트 적립

사용자가 직접 호출하는 포인트 적립 API는 제공하지 않는다. 포인트 증감은 다른 도메인 서비스가 내부 포인트 서비스 메서드를 호출해 처리한다.

내부 서비스 후보:

```java
public void earnPoint(Long userId, int amount, PointReasonType reasonType, String relatedType, Long relatedId);
public void usePoint(Long userId, int amount, PointReasonType reasonType, String relatedType, Long relatedId);
```

처리 규칙:

- `amount`는 양수로 입력받고, 저장 시 적립은 `+amount`, 사용은 `-amount`로 기록한다.
- `user_points.current_point` 변경과 `point_history` 저장은 같은 트랜잭션에서 처리한다.
- 포인트 사용 시 잔액이 부족하면 실패 처리하고 내역을 남기지 않는다.
- 동일 이벤트 중복 적립을 막기 위해 `related_type + related_id + reason_type` 기준의 멱등성 검사를 검토한다.
- 자동 적립은 Controller를 만들지 않고 Service 계층 public method 또는 도메인 이벤트 리스너로 제공한다.
- 일일 적립 제한이 필요한 이벤트는 `points_daily_limit`를 함께 갱신한다.
- 사용자는 하루 최대 50P까지 적립할 수 있다.
- 적립 처리 전 당일 누적 적립 포인트를 조회한다.
- 적립 예정 포인트가 잔여 한도를 초과하면 잔여 한도만큼만 적립한다.
- 잔여 한도가 0인 경우 포인트를 적립하지 않는다.
- 시스템은 매년 1월 1일 사용자의 현재 보유 포인트(`current_point`)와 누적 포인트(`esg_score`)를 모두 초기화한다.
- 초기화 시 기존 보유 포인트는 포인트 이력에 `RESET` 타입으로 기록한다.
- 초기화 이후 사용자의 `current_point`와 `esg_score`는 0으로 설정된다.

## 포인트 적립 정책
| 이벤트 | 포인트 |
|---------|---------|
| 로그인 | +1 |
| 워키 첫 질문 등록 | +10 |
| 워키 질문 등록 | +5 |
| 워키 답변 등록 | +5 |
| 워키 답변 채택 | +5 |
| 티켓 답변 등록 | +15 |
| 티켓 내용 지식화 | +30 |

### 워키 첫 질문 적립 조건

- 사용자가 등록한 첫 워키 질문에만 `WORKI_FIRST_QUESTION_CREATED` 포인트를 적립한다.
- 첫 질문 여부는 포인트 적립 이력이 아니라 사용자의 워키 질문 생성 이력으로 판단한다.
- 첫 질문이 삭제되었거나 일일 적립 한도 때문에 실제 적립 포인트가 0P가 된 경우에도 이후 질문은 일반 워키 질문 등록으로 처리한다.

### 워키 답변 등록 적립 조건

- 다른 사용자의 워키 질문에 답변을 등록한 경우에만 `WORKI_ANSWER_CREATED` 포인트를 적립한다.
- 본인이 작성한 워키 질문에 직접 답변을 등록한 경우에는 답변 등록 포인트를 적립하지 않는다.
- 동일 날짜에 여러 답변을 등록해도 일일 적립 한도 50P 이내에서는 답변별로 적립할 수 있다.

### 워키 답변 채택 적립 조건

- 다른 사용자의 워키 질문에 등록한 답변이 채택된 경우에만 `WORKI_ANSWER_ACCEPTED` 포인트를 적립한다.
- 본인이 작성한 워키 질문에 본인이 등록한 답변이 채택된 경우에는 답변 채택 포인트를 적립하지 않는다.
- 동일 날짜에 여러 답변이 채택되어도 일일 적립 한도 50P 이내에서는 채택된 답변별로 적립할 수 있다.

## 데이터 모델

기존 DB 테이블:

| 테이블 | 용도 |
|---|---|
| `user_points` | 사용자별 현재 포인트, ESG 점수, 등급 참조 저장 |
| `point_history` | 포인트 적립/사용 이력 저장 |
| `points_daily_limit` | 사용자별 일일 적립량 제한 관리 |

주요 컬럼:

| 테이블 | 컬럼 | 설명 |
|---|---|---|
| `user_points` | `user_id` | 사용자 ID, PK |
| `user_points` | `current_point` | 현재 보유 포인트 |
| `user_points` | `esg_score` | ESG 점수 |
| `user_points` | `grade_id` | ESG 등급 ID |
| `point_history` | `point_amount` | 포인트 증감량. 적립은 양수, 사용은 음수 |
| `point_history` | `reason_type` | 포인트 변동 사유 |
| `point_history` | `related_type` | 관련 도메인 타입 |
| `point_history` | `related_id` | 관련 도메인 엔티티 ID |

## 구현 체크리스트

- `PointController` 경로를 신규 API 경로 기준으로 정리한다.
- `PointHistoryType` enum을 추가해 `ALL`, `EARN`, `SPEND` 필터를 명시한다.
- `PointHistoryRepository`에 타입별 조회 조건을 추가한다.
- `PointService`에 내부 적립/사용 메서드를 추가한다.
- `UserPoint`에 포인트 증가/감소 도메인 메서드를 추가한다.
- `PointHistory`에 생성 팩토리 메서드를 추가한다.
- 잔액 부족, 잘못된 타입, 잘못된 금액에 대한 에러 타입을 추가한다.
- 조회 API와 내부 적립/사용 트랜잭션 테스트를 작성한다.

## 완료 기준

- `GET /api/v1/me/points`에서 현재 사용자의 포인트가 조회된다.
- `GET /api/v1/me/point-histories`에서 `ALL`, `EARN`, `SPEND` 필터가 정상 동작한다.
- 포인트 자동 적립은 외부 API 없이 서비스 내부 호출로 처리된다.
- 포인트 잔액과 히스토리 저장이 트랜잭션으로 함께 성공하거나 함께 실패한다.
- 다른 사용자의 포인트를 임의로 조회할 수 없다.
