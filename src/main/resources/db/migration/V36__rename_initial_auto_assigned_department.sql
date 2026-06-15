ALTER TABLE tickets
    DROP FOREIGN KEY fk_tickets_initial_assigned_department;

ALTER TABLE tickets
    DROP INDEX idx_tickets_initial_assigned_department_id;

ALTER TABLE tickets
    CHANGE COLUMN initial_assigned_department_id initial_auto_assigned_department_id BIGINT NULL;

ALTER TABLE tickets
    ADD CONSTRAINT fk_tickets_initial_auto_assigned_department
        FOREIGN KEY (initial_auto_assigned_department_id)
        REFERENCES departments(department_id);

CREATE INDEX idx_tickets_initial_auto_assigned_department_id
    ON tickets (initial_auto_assigned_department_id);
