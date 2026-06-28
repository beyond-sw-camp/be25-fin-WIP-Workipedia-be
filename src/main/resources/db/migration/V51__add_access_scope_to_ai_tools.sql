ALTER TABLE ai_tools
    ADD COLUMN access_scope VARCHAR(30) NOT NULL DEFAULT 'UNRESTRICTED' AFTER response_schema,
    ADD COLUMN self_identity_param VARCHAR(100) NULL AFTER access_scope;
