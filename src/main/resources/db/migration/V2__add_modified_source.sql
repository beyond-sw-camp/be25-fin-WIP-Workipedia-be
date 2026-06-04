-- BaseTimeEntity에 modified_source 컬럼이 추가됨. BaseTimeEntity를 상속하는 엔티티의 매핑 테이블에 컬럼을 보강한다.
-- 값은 ModifiedSource enum(USER/ADMIN/CHATBOT/SYSTEM)을 문자열로 저장한다. 기존 레코드 호환을 위해 NULL 허용.

ALTER TABLE worki_questions ADD COLUMN modified_source VARCHAR(30) NULL;
ALTER TABLE worki_answers   ADD COLUMN modified_source VARCHAR(30) NULL;
ALTER TABLE reactions       ADD COLUMN modified_source VARCHAR(30) NULL;
ALTER TABLE manuals         ADD COLUMN modified_source VARCHAR(30) NULL;
