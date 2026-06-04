# Chatbot/RAG Domain Guide

> 문서 유형: Development Guide
> 상태: Draft
> 정본 위치: `docs/dev/domain-guides/chatbot-rag.md`
> 관련 문서: `docs/adr/002-rag-strategy.md`, `docs/adr/008-local-llm-security-strategy.md`, `docs/dev/harness-engineering.md`, `docs/api/api-contract.md`
> 버전: v0.2
> 최종 수정: 2026-06-04

## 개발 목표

사용자 질문에 대해 매뉴얼/워키를 검색하고, 출처가 있는 답변 또는 요청 티켓 전환 액션을 반환한다.

## 먼저 볼 문서

- `docs/adr/002-rag-strategy.md`
- `docs/adr/008-local-llm-security-strategy.md`
- `docs/dev/harness-engineering.md`
- `docs/api/api-contract.md`

## MVP 구현 범위

- 챗봇 세션 생성
- 메시지 저장
- seed 매뉴얼/워키 문서 검색
- local embedding 또는 mock embedding adapter
- top-k 검색
- 출처 포함 답변 반환
- `references` 저장
- 답변 없음/불충분 시 요청 티켓 전환 액션 반환
- 개인정보 마스킹 기본 케이스
- RAG + QLoRA 역할 분리
- base_system/admin_context 프롬프트 설정 관리
- APPROVED 지식 데이터 기반 QLoRA 파인튜닝 트리거 인터페이스
- 출처 최신성 표시

## API/DB 영향

- `chatbot_sessions`
- `chatbot_messages`
- `chatbot_messages.references_json`
- `ai_prompt_settings`
- `knowledge_candidates`
- manual/worki chunks
- embedding adapter
- chatbot query API
- Spring Boot ↔ Python AI 서버 API

## 권한/보안 체크

- 출처 없는 답변 금지
- 개인정보 저장 전 마스킹
- 외부 API 호출 기본 제외
- 근거 부족 시 그럴듯한 답변 생성 금지
- QLoRA에 지식 자체를 학습시키지 않는다.
- `base_system` 변경 시 재학습 영향 안내가 필요하다.

## 완료 기준

- 질문을 입력하면 챗봇 메시지가 저장된다.
- 근거가 있으면 매뉴얼/워키 출처와 함께 답변한다.
- 근거가 없으면 요청 티켓 전환 액션을 반환한다.
- `references`에 문서 ID, chunk ID, 제목, 링크가 남는다.
- 오래된 출처는 최신성 경고와 함께 표시된다.
- AI 서버 파인튜닝 상태 조회 API 계약이 문서화된다.

## 논의 필요 사항

- local embedding 모델 후보
- local LLM 사용 여부
- vector store 최소 구현 방식
- seed 문서 개수와 내용
- QLoRA 자동 파인튜닝을 MVP에 포함할지 여부
- 학습 데이터 유효기간 기본값
