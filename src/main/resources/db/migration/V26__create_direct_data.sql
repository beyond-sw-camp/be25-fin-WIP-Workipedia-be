-- SYSTEM_ADMIN이 직접 작성하는 수기 Q/A 지식 전용 테이블.
-- ticket 기반 지식화 데이터(knowledge_data)와 섞지 않기 위해 direct_data로 분리한다.
CREATE TABLE direct_data (
    direct_data_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content LONGTEXT NOT NULL,
    category VARCHAR(100) NULL,
    is_active CHAR(1) NOT NULL DEFAULT 'Y',
    created_by BIGINT NOT NULL,
    updated_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N',
    modified_source VARCHAR(30) NULL,
    CONSTRAINT fk_direct_data_created_by
        FOREIGN KEY (created_by)
        REFERENCES users (user_id),
    CONSTRAINT fk_direct_data_updated_by
        FOREIGN KEY (updated_by)
        REFERENCES users (user_id),
    CONSTRAINT ck_direct_data_is_active
        CHECK (is_active IN ('Y', 'N')),
    CONSTRAINT ck_direct_data_is_deleted
        CHECK (is_deleted IN ('Y', 'N'))
);

-- 관리자 목록의 soft delete/활성 상태/category 필터 조회를 위한 복합 인덱스.
CREATE INDEX idx_direct_data_deleted_active_category
    ON direct_data (deleted_at, is_active, category);
