-- admin_logs action_type CHECK constraint update for AI prompt setting changes.
-- AdminAiPromptService writes AI_PROMPT_SETTING_UPDATE audit logs when custom prompt settings are saved.
ALTER TABLE admin_logs DROP CONSTRAINT ck_admin_logs_action_type;

ALTER TABLE admin_logs ADD CONSTRAINT ck_admin_logs_action_type
    CHECK (action_type IN (
        'USER_DEACTIVATE',
        'WORKI_READ',
        'WORKI_UPDATE',
        'WORKI_DELETE',
        'MANUAL_UPDATE',
        'MANUAL_DELETE',
        'TICKET_ASSIGN',
        'TICKET_TRANSFER_REQUEST',
        'TICKET_ROUTE_OVERRIDE',
        'COMMON_QUEUE_ASSIGN',
        'KNOWLEDGE_REVIEW',
        'KNOWLEDGE_PUBLISH',
        'FLASH_CHAT_MESSAGE_DELETE',
        'FLASH_CHAT_CONFIG_UPDATE',
        'AI_TOOL_CREATE',
        'AI_TOOL_UPDATE',
        'AI_PROMPT_SETTING_UPDATE'
    ));
