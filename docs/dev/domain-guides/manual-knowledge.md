# Manual Knowledge Guide

> 문서 유형: Development Guide
> 상태: Draft
> 최종 수정: 2026-06-09

## 목적

파일 매뉴얼로 만들기에는 짧지만 챗봇이 알아야 하는 운영 정보를 SYSTEM_ADMIN이 직접 등록하고 RAG에 반영한다.

## 흐름

```text
SYSTEM_ADMIN 등록·수정
→ BE가 마스킹된 원문과 sync_status=PENDING 저장
→ 커밋 후 AI 동기화 요청
→ AI chunking/embedding/ChromaDB upsert
→ SYNCED 또는 FAILED
```

삭제 시에도 RDB soft delete 후 ChromaDB 문서를 비동기 제거한다.

## BE 책임

- 수기 지식 CRUD와 SYSTEM_ADMIN 권한
- 제목, 내용, 활성 상태, 수정자와 수정일 저장
- `PENDING`, `SYNCED`, `FAILED` 상태와 실패 사유 저장
- 동기화 재시도 API와 감사 로그

## AI 책임

- 민감정보 마스킹 검증
- chunking, embedding, ChromaDB upsert/delete
- 출처 제목과 수정일 메타데이터 구성

## 구현 전 migration

`manual_knowledge` 테이블과 Vector Store 문서 ID·동기화 상태 컬럼이 필요하다. 현재 V17까지는 아직 생성되지 않았다.
