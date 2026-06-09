-- 매뉴얼 원본 PDF 파일을 R2(Object Storage)에 보관하기 위한 컬럼 추가
-- file_key: R2 오브젝트 키 (삭제/재업로드 시 사용)
-- file_url: 파일에 접근 가능한 URL (프론트 다운로드/표시용)
ALTER TABLE manuals
    ADD COLUMN file_key VARCHAR(500) NULL AFTER source_url,
    ADD COLUMN file_url VARCHAR(500) NULL AFTER file_key;
