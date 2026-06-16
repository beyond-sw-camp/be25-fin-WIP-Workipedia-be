ALTER TABLE ticket_routing_logs
    ADD COLUMN score_margin  DECIMAL(5, 2) NULL AFTER confidence_score,
    ADD COLUMN model_version VARCHAR(100)  NULL AFTER reasons_json;
