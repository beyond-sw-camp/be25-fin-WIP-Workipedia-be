# Tool Integration Guide

> 문서 유형: Development Guide
> 상태: Draft
> 최종 수정: 2026-06-09

## 목적

고객사마다 다른 실시간 데이터 조회 기능을 API Tool 또는 제한된 DB Query Tool로 제공한다. 화면 명칭은 `Tool 관리`, 기술 명칭은 `Tool Calling`을 사용한다.

## 원칙

- 고객사가 API를 제공하면 HTTP API Tool을 우선한다.
- API가 없고 고객사 동의·네트워크·읽기 권한이 확보된 경우에만 DB Query Tool을 사용한다.
- AI는 SQL을 생성하지 않는다.
- 개발자가 SELECT 템플릿과 허용 파라미터·반환 컬럼을 검증한다.
- SYSTEM_ADMIN은 API Tool을 관리하고 승인된 DB Query Tool의 활성 상태만 변경한다.
- DB 접속정보, SQL 원문, credential은 관리자와 AI에 노출하지 않는다.

## 실행 흐름

```text
AI가 활성·승인 Tool과 인자 선택
→ AI가 POST /internal/ai-tools/{toolId}/execute 호출
→ BE가 JSON Schema와 권한 검증
→ HTTP/DB adapter 실행
→ BE가 마스킹된 파라미터와 실행 메타데이터만 감사 로그 기록
→ AI가 결과를 마스킹한 뒤 해석해 답변
```

## 관리 화면 필드

### API Tool

SYSTEM_ADMIN이 다음 항목을 등록·테스트·활성화한다.

| 필드 | 설명 |
|---|---|
| `name` | AI에 노출할 고유 Tool 이름 |
| `description` | Tool 사용 조건과 반환 정보 |
| `endpointUrl` | 고객사 API endpoint |
| `httpMethod` | 허용 HTTP method |
| `parametersSchema` | 허용 입력 파라미터 JSON Schema |
| `responseSchema` | AI에 전달할 응답 필드 범위 |
| `authType` | NONE, API_KEY, BEARER_TOKEN, OAUTH2 |
| `credentialRef` | Secret에 저장된 인증정보 참조값 |
| `timeoutMs` | 호출 제한 시간 |
| `maxResultCount` | 최대 반환 건수 |
| `active` | AI에 Tool을 노출할지 여부 |

관리 화면은 실제 credential 값을 표시하지 않고 참조 상태만 보여준다.

### DB Query Tool

개발자가 생성·검증하며 SYSTEM_ADMIN은 승인된 Tool의 활성 여부만 관리한다. SQL 원문, datasource 주소와 credential은 관리자 화면에 노출하지 않는다.

## BE 책임

- V16 `ai_tools` 기반 Tool CRUD
- credential reference와 datasource 관리
- HTTP/DB 실행
- timeout, 최대 결과 수, 허용 컬럼 제한
- 호출 감사 로그

## DB Query Tool 제한

- 고객사 API가 없는 경우에만 예외적으로 사용한다.
- 고객사 동의와 네트워크·DB 접근 승인을 먼저 확보한다.
- 운영 계정과 분리된 read-only DB 계정을 사용한다.
- `SELECT` 단일 쿼리만 허용하고 INSERT, UPDATE, DELETE, DDL, 프로시저 호출을 금지한다.
- AI가 SQL을 생성·수정하거나 자유 텍스트를 SQL 조각으로 삽입하지 못하게 한다.
- 개발자가 검증한 parameterized query template에 타입이 정의된 값만 바인딩한다.
- 허용된 View·테이블·컬럼만 조회하고 응답 허용 컬럼을 별도로 제한한다.
- 다중 statement, 주석, 동적 식별자, 서브쿼리 확장 등 우회 입력을 차단한다.
- timeout과 최대 결과 건수를 강제하고 대용량 결과를 AI에 전달하지 않는다.
- 호출자, Tool ID, 마스킹된 파라미터, 결과 건수, 실행 시간과 성공·실패 상태를 감사 로그에 남긴다.
- SQL 원문, DB 주소와 credential은 SYSTEM_ADMIN 화면과 AI 요청에 포함하지 않는다.

## 오류 상태

- 빈 결과: `NO_RESULT`
- timeout·연결 실패: `ERROR`
- 권한·스키마·마스킹 위반: `BLOCKED`
- 정상 결과: `SUCCESS`

## 구현 전 추가 migration

- `tool_call_logs`
- 필요 시 별도 credential/datasource reference 테이블
