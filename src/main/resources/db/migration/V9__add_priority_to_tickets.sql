ALTER TABLE tickets
    ADD COLUMN priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM'
        CHECK (priority IN ('MEDIUM', 'HIGH'))
        AFTER content;

CREATE INDEX idx_tickets_priority ON tickets (priority);
