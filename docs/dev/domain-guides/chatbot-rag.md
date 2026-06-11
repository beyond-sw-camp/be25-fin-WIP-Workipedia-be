# Chatbot/RAG Domain Guide

> 문서 유형: Development Guide
> 상태: Draft
> 정본 위치: `docs/dev/domain-guides/chatbot-rag.md`
> 관련 문서: `docs/adr/002-rag-strategy.md`, `docs/adr/008-local-llm-security-strategy.md`, `docs/api/api-contract.md`
> 버전: v0.4
> 최종 수정: 2026-06-11

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
- AI Vector Store는 ChromaDB, BE 검색은 Elasticsearch가 담당한다.
- 검색 후보는 Cross-Encoder로 재정렬하고 `candidate_id`, `score`, `rank`를 반환한다.

## BE 책임

- 챗봇 세션과 메시지 저장
- 인증·권한과 감사 로그
- `base_prompt`의 배포 설정 관리
- `custom_prompt` 저장과 SYSTEM_ADMIN 변경 API
- API/DB Query Tool 정의, credential reference, 승인·활성 상태 저장
- Tool HTTP/DB 실행과 감사 로그
- 수기 지식·승인 지식 CRUD와 ChromaDB 동기화 상태 저장

## AI 책임

- 민감정보 탐지와 마스킹
- 문서 chunking, embedding, retrieval
- Cross-Encoder reranking
- Tool 선택, 입력 검증, 결과 마스킹과 해석
- 매뉴얼 → 워키 → 지식 RAG → Tool 순차 폴백 오케스트레이션
- 출처 검증과 negative answer 판정

## Prompt 계약

```text
final_system_prompt = base_prompt + custom_prompt
```

- `base_prompt`: 코드/배포 설정으로 관리하며 관리자 화면에서 수정하지 않는다.
- `custom_prompt`: SYSTEM_ADMIN이 수정할 수 있고 활성 상태와 변경 이력을 저장한다.

## 지식 동기화

1. TEAM_ADMIN 승인 트랜잭션에서 `knowledge_data`와 `ai_sync_jobs` 작업을 함께 저장한다.
2. 커밋 후 `@Scheduled` 워커가 AI 동기화 API를 호출한다.
3. AI가 마스킹, chunking, embedding, ChromaDB upsert를 수행한다.
4. 성공 시 `SYNCED`, 실패 시 `FAILED`와 실패 사유를 작업 테이블에 저장한다.
5. 실패 작업은 관리자 화면 또는 배치에서 재시도한다.

## 완료 기준

- 근거가 있으면 출처와 함께 답변한다.
- 근거가 없으면 문자열 판정 없이 다음 fallback 단계로 이동한다.
- 민감정보 원문을 AI 로그, Vector Store, 외부 LLM 요청에 저장하지 않는다.
- Tool은 활성·승인된 정의와 허용 파라미터만 실행한다.
- Reranker 응답과 최종 판단에 점수와 순위가 남는다.
- 운영 경로에 mock 답변이 없다.

## 논의 필요 사항

- 문서 유형별 chunk 크기와 overlap
- RAG와 라우팅의 Cross-Encoder 임계값
- 고객사별 provider timeout과 장애 기준
