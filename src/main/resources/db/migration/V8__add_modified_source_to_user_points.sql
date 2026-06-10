-- UserPoint 엔티티가 BaseTimeEntity를 상속하며 modified_source 컬럼을 요구한다.

ALTER TABLE user_points ADD COLUMN modified_source VARCHAR(30) NULL;
