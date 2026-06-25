# Manual Page Citation Plan

> 문서 유형: Implementation Plan
> 상태: Draft
> 작성일: 2026-06-25

## 배경

현재 매뉴얼 PDF는 BE에서 텍스트를 추출한 뒤 `manuals.content`에 하나의 문자열로 저장하고, AI 동기화 시 `/documents/ingest-text`로 통째로 전달한다.

이 구조는 RAG 검색과 답변 생성은 가능하지만, PDF 원본의 파일명과 페이지 번호가 사라진다. 따라서 챗봇 답변에서 `어느 문서의 몇 페이지를 근거로 답했는지`를 정확히 표시하기 어렵다.

특히 하나의 매뉴얼에 여러 PDF가 업로드될 수 있으므로, 단순한 전역 페이지 번호만으로는 충분하지 않다.

```text
file1.pdf 3장 -> 1, 2, 3
file2.pdf 2장 -> 4, 5
```

위처럼 전역 번호만 저장하면 사용자는 `5페이지`가 `file2.pdf의 2페이지`인지 바로 알 수 없다. 답변 citation에는 반드시 원본 파일명과 원본 파일 기준 페이지 번호가 필요하다.

## 목표

- PDF를 페이지 단위로 파싱하고 BE DB에 저장한다.
- 각 페이지가 어떤 원본 파일의 몇 페이지인지 보존한다.
- AI 적재 시 페이지 메타데이터를 Qdrant payload까지 전달한다.
- 챗봇 답변 citation에서 파일명과 페이지 번호를 표시할 수 있게 한다.

## 비목표

- PDF 뷰어에서 특정 페이지로 자동 이동하는 기능은 이번 범위에서 제외한다.
- 기존 `/documents/ingest` multipart endpoint 제거는 하지 않는다.
- 기존 수기 지식, 워키 지식, 부서 라우팅 RAG 구조는 변경하지 않는다.

## 최종 Citation 형태

답변 출처는 아래처럼 표시할 수 있어야 한다.

```text
출처: 2025_Hanwha_Profile_Full_Page_KR.pdf / 12페이지
```

여러 파일을 하나의 매뉴얼로 묶은 경우에도 원본 파일 기준 페이지를 표시한다.

```text
출처: file2.pdf / 1페이지
```

필요하면 내부적으로만 전역 페이지 번호도 함께 저장한다.

```text
file1.pdf / 1페이지 / globalPage=1
file1.pdf / 2페이지 / globalPage=2
file1.pdf / 3페이지 / globalPage=3
file2.pdf / 1페이지 / globalPage=4
file2.pdf / 2페이지 / globalPage=5
```

사용자에게 보여주는 값은 `fileName + pageNumber`를 우선한다.

## BE 설계

### 1. `manual_pages` 테이블 추가

PDF 페이지별 텍스트와 원본 파일 정보를 저장한다.

예상 컬럼:

| 컬럼 | 설명 |
|---|---|
| `manual_page_id` | PK |
| `manual_id` | 매뉴얼 ID |
| `file_key` | S3 object key |
| `file_name` | 원본 파일명 |
| `file_sort_order` | 매뉴얼 안에서 파일 순서 |
| `page_number` | 원본 PDF 기준 페이지 번호 |
| `global_page_number` | 매뉴얼 전체 기준 페이지 번호 |
| `content` | 해당 페이지에서 추출한 텍스트 |
| `created_at` | 생성일 |
| `updated_at` | 수정일 |
| `deleted_at` | soft delete |

### 2. PDF 업로드 시 페이지 저장

`AdminManualService`의 PDF 등록/수정 흐름을 변경한다.

현재 흐름:

```text
PDF 업로드
-> 전체 텍스트 추출
-> manuals.content 저장
-> manual_files 저장
-> AI 동기화 작업 생성
```

변경 흐름:

```text
PDF 업로드
-> 파일별 페이지 텍스트 추출
-> manuals.content에는 전체 텍스트 유지
-> manual_files 저장
-> manual_pages에 파일별 페이지 텍스트 저장
-> AI 동기화 작업 생성
```

`manuals.content`는 목록 검색, 변경 요약, 기존 호환성을 위해 유지한다.

### 3. PDF 수정 시 페이지 교체

매뉴얼 파일이 수정되면 기존 `manual_pages`를 soft delete하고 새 페이지를 저장한다.

```text
기존 manual_pages soft delete
-> 새 PDF 페이지 파싱
-> 새 manual_pages insert
-> MANUAL UPSERT 동기화 작업 생성
```

### 4. 삭제 시 페이지도 삭제 처리

매뉴얼 삭제 시 기존처럼 `manuals`, `manual_files`를 soft delete하고, `manual_pages`도 soft delete한다.

AI 쪽 삭제는 기존 `MANUAL DELETE` 동기화 작업으로 Qdrant 문서 전체를 삭제한다.

## AI 연동 설계

### 1. page-aware ingest endpoint 추가

BE가 페이지 정보를 AI에 전달할 수 있도록 신규 endpoint를 추가한다.

```text
POST /api/v1/documents/ingest-pages
```

예상 payload:

```json
{
  "sourceId": 1,
  "sourceType": "MANUAL",
  "title": "2025 Hanwha Profile",
  "pages": [
    {
      "fileName": "file1.pdf",
      "fileKey": "manuals/1/file1.pdf",
      "fileSortOrder": 0,
      "pageNumber": 1,
      "globalPageNumber": 1,
      "text": "..."
    },
    {
      "fileName": "file2.pdf",
      "fileKey": "manuals/1/file2.pdf",
      "fileSortOrder": 1,
      "pageNumber": 1,
      "globalPageNumber": 4,
      "text": "..."
    }
  ]
}
```

### 2. Qdrant payload 확장

chunk마다 아래 메타데이터를 저장한다.

| 필드 | 설명 |
|---|---|
| `doc_id` | `MANUAL:{manualId}` |
| `source_type` | `MANUAL` |
| `source_id` | 매뉴얼 ID |
| `title` | 매뉴얼 제목 |
| `file_name` | 원본 파일명 |
| `file_key` | S3 object key |
| `file_sort_order` | 파일 순서 |
| `page_start` | 원본 PDF 기준 시작 페이지 |
| `page_end` | 원본 PDF 기준 끝 페이지 |
| `global_page_start` | 매뉴얼 전체 기준 시작 페이지 |
| `global_page_end` | 매뉴얼 전체 기준 끝 페이지 |
| `text` | chunk 원문 |

chunk가 한 파일의 여러 페이지를 걸치면 `page_start`, `page_end`를 범위로 표시한다.

chunk가 파일 경계를 넘지 않게 하는 것을 기본 정책으로 둔다. 파일이 바뀌면 chunk도 끊는다.

## Citation 정책

AI 응답에서 cited chunk를 반환할 때 다음 값을 포함한다.

```json
{
  "sourceType": "MANUAL",
  "sourceId": 1,
  "title": "2025 Hanwha Profile",
  "fileName": "file2.pdf",
  "pageStart": 1,
  "pageEnd": 2
}
```

FE 표시 규칙:

| 조건 | 표시 |
|---|---|
| `pageStart == pageEnd` | `file2.pdf / 1페이지` |
| `pageStart != pageEnd` | `file2.pdf / 1-2페이지` |
| `fileName` 없음 | 기존처럼 매뉴얼 제목만 표시 |

## 마이그레이션 전략

기존 매뉴얼은 `manual_pages`가 비어 있을 수 있다.

단계적으로 처리한다.

1. 신규 업로드부터 `manual_pages` 저장
2. 기존 매뉴얼은 수정 또는 재업로드 시 페이지 정보 생성
3. 필요하면 운영 스크립트로 기존 PDF를 다시 파싱해 `manual_pages` backfill

AI 동기화는 아래 순서로 fallback한다.

```text
manual_pages 있음 -> /documents/ingest-pages
manual_pages 없음 -> /documents/ingest-text
```

## 구현 순서

1. BE `ManualPage` entity/repository 추가
2. PDF extractor에 페이지별 추출 메서드 추가
3. 매뉴얼 등록/수정/삭제 시 `manual_pages` 저장 및 soft delete
4. BE `DocumentAiClient`에서 page-aware ingest 호출
5. AI `/documents/ingest-pages` endpoint 추가
6. AI chunk metadata에 파일명/페이지 정보 저장
7. RAG 응답 citation DTO에 파일명/페이지 정보 포함
8. FE citation 표시 확장

## 검증 항목

- 단일 PDF 업로드 시 `manual_pages.page_number`가 1부터 저장되는지 확인
- 여러 PDF 업로드 시 파일별 `page_number`와 전체 `global_page_number`가 모두 맞는지 확인
- AI 적재 후 Qdrant payload에 `file_name`, `page_start`, `page_end`가 들어가는지 확인
- 챗봇 답변 출처에 `파일명 / 페이지`가 표시되는지 확인
- 매뉴얼 삭제 후 Qdrant에서 해당 `MANUAL:{manualId}` point가 삭제되는지 확인

## 주의사항

- 사용자에게 보여주는 페이지 번호는 항상 원본 PDF 기준 `page_number`를 우선한다.
- `global_page_number`는 정렬과 내부 디버깅 용도로만 사용한다.
- 파일 경계를 넘는 chunk는 citation이 애매해지므로 피한다.
- 기존 `manuals.content`는 호환성을 위해 유지한다.
