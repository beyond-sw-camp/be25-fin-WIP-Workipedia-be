# Chatbot/RAG Domain Guide

> 문서 유형: Development Guide
> 상태: Draft
> 정본 위치: `docs/dev/domain-guides/chatbot-rag.md`
> 관련 문서: `docs/adr/002-rag-strategy.md`, `docs/adr/008-local-llm-security-strategy.md`, `docs/api/api-contract.md`
> 버전: v0.7
> 최종 수정: 2026-06-15

## 개발 목표

사용자 질문에 대해 출처가 있는 답변을 반환하고, 매뉴얼·워키·지식 RAG·Tool 순서로 안전하게 전환한다.

## 확정된 구조

폴백 순서: A 매뉴얼 → B 워키 → C 지식 RAG → D Tool Calling

```text
A. 매뉴얼 RAG
→ B. 워키 RAG
→ C. 지식 RAG
   - TEAM_ADMIN 승인 지식화 게시판(`KNOWLEDGE_DATA`)
   - SYSTEM_ADMIN 수기 지식(`MANUAL_KNOWLEDGE`)
→ D. 등록된 Tool
→ 모두 실패하면 요청 티켓 생성 전환 액션
```

- 해결된 티켓 이력은 별도 단계가 아니며 TEAM_ADMIN 승인 지식화 게시판(c)으로만 검색에 반영한다.
- `knowledge_data`와 `manual_knowledge`는 DB·`sourceType`·collection을 분리한다.
- C단계는 두 collection을 독립 조회한 뒤 후보를 합쳐 통합 reranking한다.

- LangGraph 대신 Python `for` loop와 `if-else`를 사용한다.
- 단계 결과는 `SUCCESS`, `NO_RESULT`, `ERROR`, `BLOCKED`로 반환한다.
- QLoRA와 mock 답변은 사용하지 않는다.
- AI Vector Store는 Qdrant, BE 검색은 Elasticsearch가 담당한다.
- 검색 후보는 Cross-Encoder로 재정렬하고 `candidate_id`, `score`, `rank`를 반환한다.

## BE 책임

- 챗봇 세션과 메시지 저장
- 인증·권한과 감사 로그
- `base_prompt`의 배포 설정 관리
- `custom_prompt` 저장과 SYSTEM_ADMIN 변경 API
- API/DB Query Tool 정의, credential reference, 승인·활성 상태 저장
- Tool HTTP/DB 실행과 감사 로그
- 수기 지식·승인 지식 CRUD와 Qdrant 동기화 상태 저장

## AI 책임

- 사용자에게 반환하는 최종 LLM 응답 마스킹
- 문서 chunking, embedding, retrieval
- Cross-Encoder reranking
- Tool 선택, 입력 검증과 결과 기반 답변 생성
- 매뉴얼 → 워키 → 지식 RAG → Tool 순차 폴백 오케스트레이션
- 출처 검증과 negative answer 판정

## Prompt 계약

```text
final_system_prompt = base_prompt + custom_prompt
```

- `base_prompt`: 코드/배포 설정으로 관리하며 관리자 화면에서 수정하지 않는다.
- `custom_prompt`: SYSTEM_ADMIN이 수정할 수 있고 활성 상태와 변경 이력을 저장한다.

## BE-AI 연동 계약

BE는 세션 이력을 조립해 AI의 `POST /api/v1/chat`을 호출한다.

```json
{
  "question": "현재 질문",
  "customPrompt": null,
  "sessionContext": [
    {
      "messageId": 1,
      "senderType": "USER",
      "content": "이전 질문"
    }
  ]
}
```

세션 컨텍스트 규칙:

- 같은 세션의 최근 메시지 최대 10개를 오래된 순서로 전달한다.
- `USER`, `ASSISTANT` 메시지만 포함하고 `SYSTEM`은 제외한다.
- 현재 질문은 `question`에만 넣고 `sessionContext`에 중복 포함하지 않는다.
- soft-delete된 세션과 메시지는 조회하지 않는다.

AI 응답의 핵심 필드는 `answer`, `sources`, `route`, `action`, `stepHistory`다. 출처는 `sourceType`, `sourceId`, nullable `chunkIndex`, 제목과 점수를 포함한다. BE는 답변과 출처를 세션 메시지에 저장하고, 매뉴얼 출처는 `(manualId, chunkIndex)`로 실제 청크를 식별한다. 활성 `custom_prompt`가 있으면 요청에 포함하고, 없거나 비활성이면 `null`을 전달한다.

AI 호출 실패 시 임의 답변을 생성하지 않고 서비스 이용 불가 안내를 반환한다. `CREATE_TICKET` 같은 전환 액션은 `next_action`으로 저장하며 일반 근거 답변과 구분한다.

## 세션과 메시지 저장

- `chatbot_sessions`: 사용자별 대화 세션과 제목을 저장한다.
- `chatbot_messages`: `USER`, `ASSISTANT`, `SYSTEM` 메시지와 출처 JSON, 다음 액션을 저장한다.
- 세션 목록과 메시지 목록은 본인 소유 데이터만 조회할 수 있다.
- 존재하지 않거나 삭제된 세션은 404, 다른 사용자의 세션은 403으로 처리한다.
- AI의 `sources`는 `references_json`에 직렬화해 답변 근거를 보존한다.

질문 처리 흐름:

```text
세션 소유권 확인 및 기존 context 조회
→ USER 메시지 저장
→ DB 트랜잭션 밖에서 AI 호출
→ ASSISTANT 메시지와 references/action 저장
```

AI 호출 동안 DB 트랜잭션을 유지하지 않는다. AI 호출이 실패해도 저장된 사용자 질문은 보존하고 fallback 답변을 별도 메시지로 저장한다.

### 스트리밍(SSE, 타자 효과)

`POST /chatbot/sessions/{sessionId}/messages/stream`은 답변을 토큰 단위 SSE로 흘려보낸다. 흐름은 위와 동일하되 AI 답변을 한 번에 받지 않는다.

```text
세션 소유권 확인 및 기존 context 조회 → USER 메시지 저장 (트랜잭션, 서블릿 스레드)
→ AI 서버 /api/v1/chat/stream 호출(WebClient), token 이벤트를 프론트로 중계하며 answer 누적
→ 스트림 종료 후 누적 answer + done 메타데이터로 ASSISTANT 메시지 저장 (트랜잭션, boundedElastic 스레드)
→ done 이벤트로 저장된 메시지(messageId 포함) 전달
```

- 중계 기술: `spring-boot-starter-webflux`의 `WebClient`. 컨트롤러는 `Flux<ServerSentEvent>`를 반환한다(Spring MVC가 스트리밍 처리).
- JPA 저장은 블로킹이므로 reactor 이벤트 루프가 아닌 `Schedulers.boundedElastic()`에서 실행한다.
- 빈 답변 처리·`next_action`·`references_json` 저장 로직은 일반 엔드포인트와 동일하게 `saveAssistantMessage`를 재사용한다.
- AI 스트림 실패 시 `FallbackChatbotAiClient` 안내 메시지를 저장하고 `done`으로 전달한다.
- 관련 파일: `HttpChatbotAiStreamClient`, `ChatbotStreamToken`/`ChatbotStreamDone`, `ChatbotService.sendMessageStream`, `AiClientConfig.chatbotAiWebClient`.

## 지식 동기화

1. TEAM_ADMIN 승인 트랜잭션에서 `knowledge_data`와 `ai_sync_jobs` 작업을 함께 저장한다.
2. 커밋 후 `@Scheduled` 워커가 AI 동기화 API를 호출한다.
3. AI가 원문을 chunking, embedding하여 Qdrant에 upsert한다.
4. 성공 시 `SYNCED`, 실패 시 `FAILED`와 실패 사유를 작업 테이블에 저장한다.
5. 실패 작업은 관리자 화면 또는 배치에서 재시도한다.

## 완료 기준

- 근거가 있으면 출처와 함께 답변한다.
- 근거가 없으면 문자열 판정 없이 다음 fallback 단계로 이동한다.
- BE RDB는 민감정보를 암호화 저장하고 AI는 사용자에게 반환하는 최종 LLM 응답만 마스킹한다.
- 민감정보 원문과 비밀정보를 애플리케이션 로그에 기록하지 않는다.
- Tool은 활성·승인된 정의와 허용 파라미터만 실행한다.
- Reranker 응답과 최종 판단에 점수와 순위가 남는다.
- 운영 경로에 mock 답변이 없다.

## 논의 필요 사항

- 문서 유형별 chunk 크기와 overlap
- RAG와 라우팅의 Cross-Encoder 임계값
- 고객사별 provider timeout과 장애 기준
