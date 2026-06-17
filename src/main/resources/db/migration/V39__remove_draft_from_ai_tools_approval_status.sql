UPDATE ai_tools
SET approval_status = 'APPROVED'
WHERE approval_status = 'DRAFT';

ALTER TABLE ai_tools
    MODIFY approval_status VARCHAR(30) NOT NULL DEFAULT 'APPROVED';

ALTER TABLE ai_tools
    DROP CONSTRAINT ck_ai_tools_approval_status;

ALTER TABLE ai_tools
    ADD CONSTRAINT ck_ai_tools_approval_status
        CHECK (approval_status IN ('APPROVED', 'REJECTED'));
