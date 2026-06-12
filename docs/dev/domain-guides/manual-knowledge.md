# Manual Knowledge Guide

> 문서 유형: Development Guide
> 상태: Draft
> 최종 수정: 2026-06-09

## 목적

파일 매뉴얼로 만들기에는 짧지만 챗봇이 알아야 하는 운영 정보를 SYSTEM_ADMIN이 직접 등록하고 RAG에 반영한다.

## 흐름

```text
SYSTEM_ADMIN 등록·수정
→ BE가 원문과 동기화 작업을 같은 트랜잭션으로 저장
→ `@Scheduled` 워커가 PENDING 동기화 작업을 AI 서버에 전달
→ AI가 민감정보 마스킹 후 chunking/embedding/Qdrant upsert
→ SYNCED 또는 FAILED
```

삭제 시에도 RDB soft delete 후 Qdrant 문서를 비동기 제거한다.

## BE 책임

- 수기 지식 CRUD와 SYSTEM_ADMIN 권한
- 제목, 내용, 활성 상태, 수정자와 수정일 저장
- `ai_sync_jobs`에 `PENDING`, `PROCESSING`, `SYNCED`, `FAILED` 상태와 실패 사유 저장
- 동기화 재시도 API와 감사 로그

## AI 책임

- 모델 호출 전과 Qdrant 저장 전 민감정보 마스킹
- chunking, embedding, Qdrant upsert/delete
- 출처 제목과 수정일 메타데이터 구성

## 구현 전 migration

`manual_knowledge`와 공통 `ai_sync_jobs` 테이블이 필요하다. 현재 migration에는 아직 생성되지 않았다.
