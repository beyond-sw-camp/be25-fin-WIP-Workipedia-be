-- Notification 엔티티가 BaseTimeEntity를 상속하며 modified_source 컬럼을 요구.
-- V2 작성 시점엔 Notification 엔티티가 없어 notifications 테이블이 누락됐다.

ALTER TABLE notifications ADD COLUMN modified_source VARCHAR(30) NULL;
