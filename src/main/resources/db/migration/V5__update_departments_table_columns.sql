ALTER TABLE departments
    DROP INDEX uk_departments_code,
    DROP INDEX uk_departments_name,
    CHANGE name department_name VARCHAR(100) NOT NULL,
    DROP COLUMN code,
    DROP COLUMN description,
    ADD CONSTRAINT uk_departments_department_name UNIQUE (department_name);
