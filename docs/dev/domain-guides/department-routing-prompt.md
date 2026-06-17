# Department Routing Prompt Integration Guide

> 문서 유형: Development Guide
> 상태: Draft
> 정본 위치: `docs/dev/domain-guides/department-routing-prompt.md`
> 대상: 부서 R&R 관리와 AI 연동 담당자
> 최종 수정: 2026-06-15

## 목적

SYSTEM_ADMIN의 자연어 지시를 AI가 부서별 최종 R&R 문장으로 변환하고, BE가 `department_routing_prompts`에 저장하는 계약을 정의한다.

## 운영 계약

```text
관리자 지시 + 활성 부서 + 현재 R&R
→ AI `POST /api/v1/department/routing-prompt`
→ 변경할 부서별 최종 routingPrompt 반환
→ BE 권한 검증·저장·감사 로그
```

- AI는 입력 문자열을 잘라 붙이지 않고 수정·추가·삭제 의도를 반영한 최종 문장을 반환한다.
- 응답에 없는 부서는 BE가 변경하지 않는다.
- 민감정보가 포함된 결과는 저장하지 않는다.
- AI 호출 실패 시 임시 문장이나 mock 결과를 저장하지 않고 구조화된 오류를 반환한다.

## API 계약

BE가 AI 서버에 다음 형식으로 요청한다.

`POST /api/v1/department/routing-prompt`

Request:

```json
{
  "instruction": "개발 2팀에 RAG도 추가해줘",
  "targets": [
    {
      "departmentId": 1,
      "departmentName": "개발 1팀",
      "currentPrompt": "개발 1팀은 ERP를 담당한다."
    },
    {
      "departmentId": 2,
      "departmentName": "개발 2팀",
      "currentPrompt": "개발 2팀은 검색을 담당한다."
    }
  ]
}
```

Response:

```json
{
  "results": [
    {
      "departmentId": 2,
      "routingPrompt": "개발 2팀은 검색과 RAG를 담당한다."
    }
  ]
}
```

- 변경이 필요한 부서만 응답에 포함한다.
- 존재하지 않는 부서 ID는 오류로 처리한다.
- 응답에 없는 부서는 기존 R&R을 유지한다.

## 기대 동작

AI는 입력 문장을 그대로 잘라 붙이지 않고 현재 R&R을 기준으로 추가·수정·삭제 의도를 반영한 최종 문장을 반환한다.

기존 R&R:

```text
개발 2팀은 검색과 QLoRA를 담당한다.
```

관리자 입력:

```text
개발 2팀에서 QLoRA는 빼고 RAG를 추가해줘
```

AI 응답:

```json
{
  "results": [
    {
      "departmentId": 2,
      "routingPrompt": "개발 2팀은 검색과 RAG를 담당한다."
    }
  ]
}
```

## 현재 코드 메모

현재 BE에는 `DepartmentRoutingPromptEditor`와 문자열 기반 `FallbackRoutingPromptEditor`가 존재한다. 이는 AI 연동 전 개발용 구현이며 운영 목표가 아니다. AI API 연동이 완료되면 동일 인터페이스의 AI adapter로 교체한다.

관련 코드:

- `src/main/java/com/wip/workipedia/department/ai/DepartmentRoutingPromptEditor.java`
- `src/main/java/com/wip/workipedia/department/ai/FallbackRoutingPromptEditor.java`
- `src/main/java/com/wip/workipedia/department/service/DepartmentService.java`

## R&R 지식 동기화

R&R 저장 후 변경된 부서마다 `ai_sync_jobs` 작업을 생성한다. 저장 트랜잭션 안에서는 AI 서버를 직접 호출하지 않고, 커밋 이후 텍스트 계열 `@Scheduled` 워커가 AI 지식 동기화 API를 호출한다.

`POST /api/v1/knowledge/sync`

```json
{
  "sourceId": 2,
  "sourceType": "DEPT_RR",
  "title": "개발팀",
  "content": "개발팀은 RAG를 담당한다.",
  "departmentId": 2,
  "departmentName": "개발팀"
}
```

- `DEPT_RR`은 `sourceId`와 `departmentId`가 같아야 한다.
- 여러 부서가 변경되면 부서별로 `DEPT_RR` `UPSERT` 작업을 만든다.
- 동기화 실패 시 R&R 변경 자체를 롤백하지 않고 `ai_sync_jobs`에 실패 사유와 재시도 시각을 남긴다.
- 부서 삭제 시 `DEPT_RR` `DELETE` 작업을 만들고 워커가 `DELETE /api/v1/knowledge/{departmentId}?sourceType=DEPT_RR`을 호출한다.

텍스트를 직접 수정할 때는 다음 관리 API를 사용하며 저장 후 동일하게 동기화한다.

`PATCH /api/v1/admin/departments/{departmentId}/routing-prompt`

## 완료 기준

- 추가·수정·삭제 지시가 부서별 최종 R&R로 반영된다.
- 존재하지 않는 부서 ID와 빈 R&R을 거부한다.
- AI 오류 시 기존 R&R을 보존한다.
- R&R 저장·삭제와 `ai_sync_jobs` 생성이 하나의 트랜잭션으로 처리된다.
- 변경 작업이 `admin_logs`에 기록된다.
