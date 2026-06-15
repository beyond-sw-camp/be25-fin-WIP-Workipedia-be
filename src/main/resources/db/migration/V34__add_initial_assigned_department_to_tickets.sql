ALTER TABLE tickets
    ADD COLUMN initial_assigned_department_id BIGINT NULL AFTER assigned_department_id;

UPDATE tickets
SET initial_assigned_department_id = assigned_department_id
WHERE initial_assigned_department_id IS NULL
  AND assigned_department_id IS NOT NULL
  AND routing_decision = 'AUTO_ASSIGNED'
  AND deleted_at IS NULL
  AND is_deleted = 'N';

ALTER TABLE tickets
    ADD CONSTRAINT fk_tickets_initial_assigned_department
        FOREIGN KEY (initial_assigned_department_id)
        REFERENCES departments(department_id);

CREATE INDEX idx_tickets_initial_assigned_department_id
    ON tickets (initial_assigned_department_id);
