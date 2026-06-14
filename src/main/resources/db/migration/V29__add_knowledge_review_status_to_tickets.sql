ALTER TABLE tickets
    ADD COLUMN knowledge_review_status VARCHAR(20) NULL AFTER completed_at,
    ADD COLUMN knowledge_reviewed_by BIGINT NULL AFTER knowledge_review_status,
    ADD COLUMN knowledge_reviewed_at DATETIME NULL AFTER knowledge_reviewed_by;

ALTER TABLE tickets
    ADD CONSTRAINT ck_tickets_knowledge_review_status
        CHECK (knowledge_review_status IS NULL OR knowledge_review_status IN ('PENDING', 'APPROVED', 'REJECTED'));
