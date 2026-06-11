-- Add explicit point history change type for earn, spend, and reset histories.
ALTER TABLE point_history
    ADD COLUMN type VARCHAR(20) NULL AFTER point_amount;

UPDATE point_history
SET type = CASE
    WHEN point_amount < 0 THEN 'SPEND'
    ELSE 'EARN'
END
WHERE type IS NULL;

ALTER TABLE point_history
    MODIFY COLUMN type VARCHAR(20) NOT NULL;

ALTER TABLE point_history
    ADD CONSTRAINT ck_point_history_type
        CHECK (type IN ('EARN', 'SPEND', 'RESET'));

CREATE INDEX idx_point_history_type ON point_history (type);
