ALTER TABLE tickets
    ADD COLUMN assigned_at DATETIME NULL AFTER assigned_department_id;