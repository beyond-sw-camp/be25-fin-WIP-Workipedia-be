-- admin_logs action_type CHECK 제약에 PROMOTE_TO_TEAM_ADMIN 추가
-- AdminUserService.promoteToTeamAdmin()이 해당 action_type으로 로그를 저장하지만
-- 기존 제약에 값이 없어 팀관리자 승격 시 CHECK 제약 위반으로 500이 발생함
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
        'AI_PROMPT_SETTING_UPDATE',
        'PROMOTE_TO_TEAM_ADMIN'
    ));
