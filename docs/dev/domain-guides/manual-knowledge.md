# Manual Knowledge Guide

> 문서 유형: Development Guide
> 상태: Draft
> 최종 수정: 2026-06-15

## 목적

파일 매뉴얼로 만들기에는 짧지만 챗봇이 알아야 하는 운영 정보를 SYSTEM_ADMIN이 직접 등록하고 RAG에 반영한다.

## 흐름

```text
SYSTEM_ADMIN 등록·수정
→ BE가 원문과 동기화 작업을 같은 트랜잭션으로 저장
→ `@Scheduled` 워커가 PENDING 동기화 작업을 AI 서버에 전달
→ AI가 원문을 chunking/embedding하여 Qdrant upsert
→ SYNCED 또는 FAILED
```

삭제 시에도 RDB soft delete 후 Qdrant 문서를 비동기 제거한다.

## BE 책임

- 수기 지식 CRUD와 SYSTEM_ADMIN 권한
- 제목, 내용, 활성 상태, 수정자와 수정일 저장
- `ai_sync_jobs`에 `PENDING`, `PROCESSING`, `SYNCED`, `FAILED` 상태와 실패 사유 저장
- 동기화 재시도 API와 감사 로그

## AI 책임

- chunking, embedding, Qdrant upsert/delete
- 출처 제목과 수정일 메타데이터 구성

## 보안

- BE RDB는 민감정보를 암호화 저장하고 사용 시 복호화한다.
- Vector Store는 검색 품질을 위해 원문을 저장하며 접근을 내부망과 서비스 계정으로 제한한다.
- 사용자에게 반환하는 최종 LLM 응답 마스킹은 AI 챗봇 서비스가 담당한다.
- 원문과 비밀정보를 로그에 기록하지 않는다.

## 구현 전 migration

`manual_knowledge`와 공통 `ai_sync_jobs` 테이블이 필요하다. 현재 migration에는 아직 생성되지 않았다.
