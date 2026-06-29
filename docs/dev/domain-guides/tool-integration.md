# Tool Integration Guide

> 문서 유형: Development Guide
> 상태: Draft
> 최종 수정: 2026-06-29

## 목적

고객사마다 다른 실시간 데이터 조회 기능을 API Tool 또는 제한된 DB Query Tool로 제공한다. 화면 명칭은 `Tool 관리`, 기술 명칭은 `Tool Calling`을 사용한다.

## 원칙

- 고객사가 API를 제공하면 HTTP API Tool을 우선한다.
- API가 없고 고객사 동의·네트워크·읽기 권한이 확보된 경우에만 DB Query Tool을 사용한다.
- AI는 SQL을 생성하지 않는다.
- SYSTEM_ADMIN(지원팀·개발팀 권한을 함께 가진 운영자)이 SELECT 템플릿과 허용 파라미터·반환 컬럼을 검증하며 등록한다.
- Tool은 등록 즉시 승인(APPROVED) 상태이며, SYSTEM_ADMIN은 이후 활성 상태만 별도로 변경한다.
- Tool은 `READ_ONLY`, `MUTATING`으로 구분하며 AI 자동 실행에는 `READ_ONLY`만 노출한다.
- 변경형 Tool은 내부 AI 실행 API에서 거부하며 별도 사용자 확인 API가 구현되기 전까지 실행하지 않는다.
- `MUTATING` HTTP Tool은 상태 점검 자체가 변경 요청을 발생시킬 수 있으므로 자동 health check도 실행하지 않는다.
- DB 접속정보, SQL 원문, credential은 관리자와 AI에 노출하지 않는다.

## 실행 흐름

```text
AI가 캐시된 활성·승인 READ_ONLY Tool과 인자 선택
→ AI가 POST /internal/ai-tools/{toolId}/execute 호출
→ AI가 인증 사용자에게서 받은 callerEmployeeId를 요청 body로 전달
→ BE가 JSON Schema와 권한 검증 및 SELF_ONLY 식별자 강제 주입
→ HTTP/DB adapter 실행
→ BE가 민감정보 원문을 제외한 실행 메타데이터만 감사 로그 기록
→ AI가 결과를 근거로 답변 생성
→ 사용자에게 반환하는 최종 답변 마스킹
```

## 관리 화면 필드

### API Tool

SYSTEM_ADMIN이 다음 항목을 등록·테스트·활성화한다.

| 필드 | 설명 |
|---|---|
| `name` | AI에 노출할 고유 Tool 이름 |
| `description` | Tool 사용 조건과 반환 정보 |
| `sideEffectType` | `READ_ONLY` 또는 `MUTATING` |
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

SYSTEM_ADMIN이 SELECT 템플릿을 검증해서 직접 생성하며, 생성 즉시 승인 상태가 된다. SYSTEM_ADMIN은 이후 활성 여부만 별도로 관리한다. SQL 원문, datasource 주소와 credential은 관리자 화면에 노출하지 않는다.

## BE 책임

- V16 `ai_tools` 기반 Tool CRUD
- credential reference와 datasource 관리
- HTTP/DB 실행
- timeout, 최대 결과 수, 허용 컬럼 제한
- 호출 감사 로그
- 활성 READ_ONLY Tool 목록 캐싱과 관리자 변경 시 캐시 삭제

## DB Query Tool 제한

- 고객사 API가 없는 경우에만 예외적으로 사용한다.
- 고객사 동의와 네트워크·DB 접근 승인을 먼저 확보한다.
- 운영 계정과 분리된 read-only DB 계정을 사용한다.
- `SELECT` 단일 쿼리만 허용하고 INSERT, UPDATE, DELETE, DDL, 프로시저 호출을 금지한다.
- `sideEffectType=READ_ONLY`만 허용한다.
- AI가 SQL을 생성·수정하거나 자유 텍스트를 SQL 조각으로 삽입하지 못하게 한다.
- 개발자가 검증한 parameterized query template에 타입이 정의된 값만 바인딩한다.
- 허용된 View·테이블·컬럼만 조회하고 응답 허용 컬럼을 별도로 제한한다.
- 다중 statement, 주석, 동적 식별자, 서브쿼리 확장 등 우회 입력을 차단한다.
- timeout과 최대 결과 건수를 강제하고 대용량 결과를 AI에 전달하지 않는다.
- 호출자, Tool ID, 결과 건수, 실행 시간과 성공·실패 상태를 감사 로그에 남긴다.
- 입력 파라미터는 업무상 필요한 최소 필드만 기록하며 민감정보 원문은 남기지 않는다.
- SQL 원문, DB 주소와 credential은 SYSTEM_ADMIN 화면과 AI 요청에 포함하지 않는다.

## 호출자 본인 제한

- `accessScope=SELF_ONLY` Tool은 `selfIdentityParam`을 필수로 지정한다.
- AI가 전달한 일반 `parameters` 안의 동일 키는 신뢰하지 않고 BE가 `callerEmployeeId`로 덮어쓴다.
- `callerEmployeeId`가 없거나 `selfIdentityParam` 설정이 잘못된 요청은 실행하지 않는다.
- 호출자 사번 원문은 Tool 실행 감사 로그에 남기지 않는다.

## 활성 Tool 캐시

- `/internal/ai-tools/active` 결과는 Redis에 5분간 캐싱한다.
- Tool 등록·수정·승인·활성 상태 변경 후 `aiTool:active` 캐시 전체를 삭제한다.
- 캐시와 실행 시점 사이에 상태가 바뀔 수 있으므로 실행 API에서도 활성·승인·READ_ONLY 여부를 다시 검증한다.

## 오류 상태

- 빈 결과: `NO_RESULT`
- timeout·연결 실패: `ERROR`
- 권한·스키마·마스킹 위반: `BLOCKED`
- 정상 결과: `SUCCESS`

## 구현 전 추가 migration

- `tool_call_logs`
- 필요 시 별도 credential/datasource reference 테이블
