ALTER TABLE departments
    DROP INDEX uk_departments_department_name,
    MODIFY department_name VARCHAR(50) NOT NULL,
    ADD COLUMN active_department_name VARCHAR(50)
        GENERATED ALWAYS AS (
            CASE
                WHEN deleted_at IS NULL THEN department_name
                ELSE NULL
            END
        ) STORED;

CREATE UNIQUE INDEX uk_departments_active_department_name
    ON departments (active_department_name);

ALTER TABLE department_routing_prompts
    DROP INDEX uk_department_routing_prompts_department,
    ADD COLUMN active_department_id BIGINT
        GENERATED ALWAYS AS (
            CASE
                WHEN deleted_at IS NULL THEN department_id
                ELSE NULL
            END
        ) STORED;

CREATE UNIQUE INDEX uk_department_routing_prompts_active_department
    ON department_routing_prompts (active_department_id);
