ALTER TABLE ai_tools
    ADD COLUMN side_effect_type VARCHAR(30) NOT NULL DEFAULT 'READ_ONLY' AFTER tool_type;

UPDATE ai_tools
SET side_effect_type = 'MUTATING'
WHERE tool_type = 'HTTP_API'
  AND http_method IN ('POST', 'PUT', 'PATCH', 'DELETE');

ALTER TABLE ai_tools
    ADD CONSTRAINT ck_ai_tools_side_effect_type
        CHECK (side_effect_type IN ('READ_ONLY', 'MUTATING')),
    ADD CONSTRAINT ck_ai_tools_db_query_read_only
        CHECK (tool_type <> 'DB_QUERY' OR side_effect_type = 'READ_ONLY');

CREATE INDEX idx_ai_tools_ai_active
    ON ai_tools (is_active, approval_status, side_effect_type, is_deleted);
