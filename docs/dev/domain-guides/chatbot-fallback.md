# Chatbot Fallback Guide

> 문서 유형: Development Guide
> 상태: Draft
> 대상: 챗봇/RAG 담당자
> 관련 코드:
> - `src/main/java/com/wip/workipedia/department/ai/DepartmentRoutingPromptEditor.java`
> - `src/main/java/com/wip/workipedia/department/ai/FallbackRoutingPromptEditor.java`
> - `src/main/java/com/wip/workipedia/department/service/DepartmentService.java`

## 목적

관리자 설정의 부서 역할 설명 입력 기능은 현재 AI 구현체가 아니라 fallback 구현체로 동작한다.

이 문서는 챗봇/RAG 담당자가 나중에 실제 AI 기반 구현체를 붙일 때 무엇을 교체해야 하는지 설명한다.

## 현재 흐름

관리자는 관리자 설정 화면의 공용 입력창에 자연어로 부서 역할 설명을 입력한다.

예:

```text
개발 1팀은 ERP와 IT를 담당하고 개발 2팀은 RAG와 QLOLA를 담당한다
```

백엔드 흐름은 다음과 같다.

1. `DepartmentService.editRoutingPrompts()`가 삭제되지 않은 부서 목록을 조회한다.
2. 각 부서의 기존 `routingPrompt`를 함께 조회한다.
3. 부서 목록과 관리자 입력 문장을 `DepartmentRoutingPromptEditor`에 전달한다.
4. editor가 부서별 최종 prompt를 반환한다.
5. 서비스가 반환된 결과를 `department_routing_prompts`에 저장한다.

## 현재 Fallback 구현체

현재 구현체는 `FallbackRoutingPromptEditor`다.

이 구현체는 AI가 아니라 단순 문자열 기반 fallback이다.

현재 동작:

- 입력 문장에서 부서명을 찾는다.
- 부서명이 나온 위치를 기준으로 문장을 자른다.
- 잘라낸 문장을 해당 부서의 routing prompt로 저장한다.
- 기존 prompt가 있으면 같은 줄 중복을 제외하고 이어 붙인다.
- 부서명이 겹칠 경우 같은 위치에서는 긴 부서명을 우선한다.

예:

```text
IT지원팀은 VPN과 계정 문의를 담당하고 IT팀은 사내 장비를 담당한다
```

`IT지원팀`, `IT팀`이 모두 존재하면 `IT지원팀`을 먼저 매칭한다.

## Fallback의 한계

Fallback은 자연어 의도를 완전히 이해하지 못한다.

처리 가능한 입력:

```text
개발 1팀은 ERP 담당이고 개발 2팀은 RAG 담당이야
```

처리가 어려운 입력:

```text
전에 말한 개발 2팀 역할에서 QLOLA는 빼고 RAG만 남겨줘
```

```text
ERP는 개발 1팀, RAG랑 QLOLA는 개발 2팀에 추가해줘
```

이런 입력은 단순 문자열 분리만으로는 정확한 최종 prompt를 만들기 어렵다.

따라서 실제 자연어 추가/삭제/수정 의도 처리는 AI 구현체가 담당해야 한다.

## AI 구현체가 해야 할 일

AI 구현체는 `DepartmentRoutingPromptEditor`를 구현하면 된다.

```java
public interface DepartmentRoutingPromptEditor {

	List<RoutingPromptEditResult> edit(
		List<RoutingPromptEditTarget> targets,
		String instruction
	);
}
```

입력값:

- `targets`: 현재 활성 부서 목록과 각 부서의 기존 prompt
- `instruction`: 관리자가 입력한 자연어 명령

`RoutingPromptEditTarget`:

```java
public record RoutingPromptEditTarget(
	Long departmentId,
	String departmentName,
	String currentPrompt
) {
}
```

반환값:

- 변경이 필요한 부서별 최종 routing prompt

`RoutingPromptEditResult`:

```java
public record RoutingPromptEditResult(
	Long departmentId,
	String routingPrompt
) {
}
```

## AI 구현체 기대 동작

AI는 입력 문장을 그대로 잘라 붙이는 것이 아니라, 부서별 최종 역할 설명을 만들어야 한다.

예:

기존 상태:

```text
개발 1팀 currentPrompt = 개발 1팀은 ERP를 담당한다.
개발 2팀 currentPrompt = 개발 2팀은 QLOLA를 담당한다.
```

관리자 입력:

```text
개발 2팀에 RAG도 추가해줘
```

AI 반환 기대값:

```json
[
  {
    "departmentId": 2,
    "routingPrompt": "개발 2팀은 QLOLA와 RAG를 담당한다."
  }
]
```

삭제/수정 입력 예:

```text
개발 2팀에서 QLOLA는 빼고 RAG만 담당하게 해줘
```

AI 반환 기대값:

```json
[
  {
    "departmentId": 2,
    "routingPrompt": "개발 2팀은 RAG를 담당한다."
  }
]
```

## 구현 권장 구조

새 구현체 예:

```java
@Component
public class AiDepartmentRoutingPromptEditor implements DepartmentRoutingPromptEditor {

	@Override
	public List<RoutingPromptEditResult> edit(
		List<RoutingPromptEditTarget> targets,
		String instruction
	) {
		// 1. targets와 instruction을 AI 서버에 전달
		// 2. AI 응답 JSON 파싱
		// 3. RoutingPromptEditResult 목록으로 변환
	}
}
```

운영 권장:

- AI 구현체를 기본 구현체로 사용한다.
- AI 호출 실패 시 `FallbackRoutingPromptEditor`를 backup으로 사용한다.
- AI 응답은 반드시 서버에서 검증한다.

## AI 응답 검증 기준

AI 응답을 그대로 저장하지 말고 다음을 검증해야 한다.

- `departmentId`가 현재 활성 부서 목록에 존재하는지
- `routingPrompt`가 비어 있지 않은지
- 응답에 없는 부서는 변경하지 않는지
- 너무 긴 prompt를 제한할지 여부
- 민감정보가 포함되지 않았는지

## 저장 책임

AI 구현체는 DB에 직접 저장하지 않는다.

저장은 `DepartmentService`가 담당한다.

AI 구현체의 책임은 오직 다음이다.

```text
현재 부서 목록 + 기존 prompt + 관리자 입력
-> 변경할 부서별 최종 prompt 목록
```

## 주의 사항

- 현재 fallback은 임시 구현체다.
- 부서 routing prompt는 티켓 자동 배정/RAG 검색에 사용될 수 있으므로 문장은 명확하고 짧게 유지하는 것이 좋다.
- 특정 부서명을 찾지 못하면 서비스는 `BAD_REQUEST`를 반환한다.
- Apidog에서 request/response body 상세를 관리하므로 API 문서에는 목록 중심으로 유지한다.
