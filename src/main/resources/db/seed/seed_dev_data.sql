-- ============================================================
-- Workipedia local API test seed data
-- Manual script. Flyway does not run files under db/seed.
--
-- Run:
--   mariadb -u wip -p workipedia < src/main/resources/db/seed/seed_dev_data.sql
--
-- Login password for every seed user:
--   Test1234!
--
-- Fixed IDs:
--   departments: 9001..9004
--   users:       9001 SYSTEM_ADMIN, 9002 TEAM_ADMIN, 9003 USER, 9004 USER
--   worki:       question 9001, answer 9001
--   ticket:      9001
--   knowledge:   9001
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

UPDATE worki_questions
SET accepted_answer_id = NULL
WHERE question_id IN (9001, 9002);

DELETE FROM knowledge_data WHERE knowledge_data_id IN (9001, 9002) OR ticket_id IN (9001, 9002);
DELETE FROM manual_citations WHERE source_id IN (9001, 9002) OR manual_id IN (9001, 9002);
DELETE FROM worki_chunks WHERE question_id IN (9001, 9002) OR answer_id IN (9001, 9002);
DELETE FROM manual_chunks WHERE manual_id IN (9001, 9002);
DELETE FROM manual_versions WHERE manual_id IN (9001, 9002) OR user_id IN (9001, 9002, 9003, 9004);
DELETE FROM manuals WHERE manual_id IN (9001, 9002);

DELETE FROM notifications WHERE user_id IN (9001, 9002, 9003, 9004) OR target_id IN (9001, 9002);
DELETE FROM point_history WHERE user_id IN (9001, 9002, 9003, 9004);
DELETE FROM points_daily_limit WHERE user_id IN (9001, 9002, 9003, 9004);
DELETE FROM user_points WHERE user_id IN (9001, 9002, 9003, 9004);
DELETE FROM admin_logs WHERE actor_id IN (9001, 9002, 9003, 9004) OR target_id IN (9001, 9002, 9003, 9004);

DELETE FROM ticket_routing_logs WHERE ticket_id IN (9001, 9002);
DELETE FROM ticket_assignments WHERE ticket_id IN (9001, 9002);
DELETE FROM ticket_transfer_requests WHERE ticket_id IN (9001, 9002);
DELETE FROM ticket_status_logs WHERE ticket_id IN (9001, 9002);
DELETE FROM ticket_answers WHERE ticket_id IN (9001, 9002) OR author_id IN (9001, 9002, 9003, 9004);
DELETE FROM tickets WHERE ticket_id IN (9001, 9002);

DELETE FROM reactions WHERE user_id IN (9001, 9002, 9003, 9004) OR target_id IN (9001, 9002);
DELETE FROM worki_answers WHERE answer_id IN (9001, 9002) OR question_id IN (9001, 9002);
DELETE FROM worki_questions WHERE question_id IN (9001, 9002);

DELETE FROM chatbot_messages
WHERE message_id IN (9001, 9002)
   OR session_id IN (9001, 9002)
   OR source_worki_question_id IN (9001, 9002)
   OR source_ticket_id IN (9001, 9002);
DELETE FROM chatbot_sessions WHERE session_id IN (9001, 9002) OR user_id IN (9001, 9002, 9003, 9004);

DELETE FROM routing_rules WHERE rule_id IN (9001, 9002, 9003, 9004);
DELETE FROM department_routing_prompts WHERE routing_prompt_id IN (9001, 9002, 9003, 9004);

DELETE FROM users WHERE user_id IN (9001, 9002, 9003, 9004);
DELETE FROM departments WHERE department_id IN (9001, 9002, 9003, 9004);
DELETE FROM esg_grade WHERE grade_id IN (9001, 9002, 9003);

SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- Departments
-- ------------------------------------------------------------
INSERT INTO departments
    (department_id, department_name, created_at, updated_at, deleted_at, is_deleted)
VALUES
    (9001, 'Seed HR', NOW(), NOW(), NULL, 'N'),
    (9002, 'Seed General Affairs', NOW(), NOW(), NULL, 'N'),
    (9003, 'Seed IT Support', NOW(), NOW(), NULL, 'N'),
    (9004, 'Seed Development', NOW(), NOW(), NULL, 'N')
ON DUPLICATE KEY UPDATE
    department_name = VALUES(department_name),
    updated_at = NOW(),
    deleted_at = NULL,
    is_deleted = 'N';

-- ------------------------------------------------------------
-- Users
-- password = BCrypt('Test1234!')
-- ------------------------------------------------------------
INSERT INTO users
    (user_id, department_id, employee_id, email, password, nickname, role, status,
     last_login_at, created_at, updated_at, deleted_at, is_deleted)
VALUES
    (9001, 9003, 'SEED_ADMIN', 'seed.admin@workipedia.local',
     '$2a$10$q7AJJ7UKfbkljvBlGDzTj.apD0pB2N3z8eGuut1BjR98y5k.pQh4O',
     'seed-admin', 'SYSTEM_ADMIN', 'ACTIVE', NULL, NOW(), NOW(), NULL, 'N'),
    (9002, 9001, 'SEED_TEAM_ADMIN', 'seed.team.admin@workipedia.local',
     '$2a$10$q7AJJ7UKfbkljvBlGDzTj.apD0pB2N3z8eGuut1BjR98y5k.pQh4O',
     'seed-team-admin', 'TEAM_ADMIN', 'ACTIVE', NULL, NOW(), NOW(), NULL, 'N'),
    (9003, 9001, 'SEED_USER', 'seed.user@workipedia.local',
     '$2a$10$q7AJJ7UKfbkljvBlGDzTj.apD0pB2N3z8eGuut1BjR98y5k.pQh4O',
     'seed-user', 'USER', 'ACTIVE', NULL, NOW(), NOW(), NULL, 'N'),
    (9004, 9004, 'SEED_DEV_USER', 'seed.dev.user@workipedia.local',
     '$2a$10$q7AJJ7UKfbkljvBlGDzTj.apD0pB2N3z8eGuut1BjR98y5k.pQh4O',
     'seed-dev-user', 'USER', 'ACTIVE', NULL, NOW(), NOW(), NULL, 'N')
ON DUPLICATE KEY UPDATE
    department_id = VALUES(department_id),
    email = VALUES(email),
    password = VALUES(password),
    nickname = VALUES(nickname),
    role = VALUES(role),
    status = VALUES(status),
    updated_at = NOW(),
    deleted_at = NULL,
    is_deleted = 'N';

-- ------------------------------------------------------------
-- Admin department routing data
-- ------------------------------------------------------------
INSERT INTO department_routing_prompts
    (routing_prompt_id, department_id, prompt_content, is_active, created_by, updated_by,
     created_at, updated_at, deleted_at, is_deleted)
VALUES
    (9001, 9001, 'Route HR, leave, and attendance questions to Seed HR.', 'Y', 9001, 9001, NOW(), NOW(), NULL, 'N'),
    (9002, 9002, 'Route supplies, assets, and general affairs questions to Seed General Affairs.', 'Y', 9001, 9001, NOW(), NOW(), NULL, 'N'),
    (9003, 9003, 'Route accounts, PCs, and network questions to Seed IT Support.', 'Y', 9001, 9001, NOW(), NOW(), NULL, 'N'),
    (9004, 9004, 'Route ERP, groupware, and development questions to Seed Development.', 'Y', 9001, 9001, NOW(), NOW(), NULL, 'N')
ON DUPLICATE KEY UPDATE
    prompt_content = VALUES(prompt_content),
    is_active = 'Y',
    updated_by = 9001,
    updated_at = NOW(),
    deleted_at = NULL,
    is_deleted = 'N';

INSERT INTO routing_rules
    (rule_id, rule_name, keywords, target_department_id, active,
     created_by, updated_by, created_at, updated_at, deleted_at, is_deleted)
VALUES
    (9001, 'Seed HR keywords', 'leave,vacation,attendance', 9001, TRUE, 9001, 9001, NOW(), NOW(), NULL, 'N'),
    (9002, 'Seed general affairs keywords', 'supplies,assets,purchase', 9002, TRUE, 9001, 9001, NOW(), NOW(), NULL, 'N'),
    (9003, 'Seed IT keywords', 'login,password,computer,network', 9003, TRUE, 9001, 9001, NOW(), NOW(), NULL, 'N'),
    (9004, 'Seed development keywords', 'ERP,groupware,development', 9004, TRUE, 9001, 9001, NOW(), NOW(), NULL, 'N')
ON DUPLICATE KEY UPDATE
    keywords = VALUES(keywords),
    target_department_id = VALUES(target_department_id),
    active = TRUE,
    updated_by = 9001,
    updated_at = NOW(),
    deleted_at = NULL,
    is_deleted = 'N';

-- ------------------------------------------------------------
-- ESG / point data
-- ------------------------------------------------------------
INSERT INTO esg_grade
    (grade_id, grade_name, min_score, max_score, grade_image_url, created_at, updated_at, deleted_at, is_deleted)
VALUES
    (9001, 'Seed Bronze', 0, 999, NULL, NOW(), NOW(), NULL, 'N'),
    (9002, 'Seed Silver', 1000, 2999, NULL, NOW(), NOW(), NULL, 'N'),
    (9003, 'Seed Gold', 3000, NULL, NULL, NOW(), NOW(), NULL, 'N')
ON DUPLICATE KEY UPDATE
    min_score = VALUES(min_score),
    max_score = VALUES(max_score),
    updated_at = NOW(),
    deleted_at = NULL,
    is_deleted = 'N';

INSERT INTO user_points
    (user_id, grade_id, current_point, esg_score, created_at, updated_at, deleted_at, is_deleted, modified_source)
VALUES
    (9001, 9003, 3500, 3500, NOW(), NOW(), NULL, 'N', 'SYSTEM'),
    (9002, 9002, 1800, 1800, NOW(), NOW(), NULL, 'N', 'SYSTEM'),
    (9003, 9001, 120, 120, NOW(), NOW(), NULL, 'N', 'SYSTEM'),
    (9004, 9001, 80, 80, NOW(), NOW(), NULL, 'N', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    grade_id = VALUES(grade_id),
    current_point = VALUES(current_point),
    esg_score = VALUES(esg_score),
    updated_at = NOW(),
    deleted_at = NULL,
    is_deleted = 'N',
    modified_source = 'SYSTEM';

-- ------------------------------------------------------------
-- Chatbot / Worki data
-- ------------------------------------------------------------
INSERT INTO chatbot_sessions
    (session_id, user_id, title, created_at, updated_at, deleted_at, is_deleted)
VALUES
    (9001, 9003, 'Seed chatbot session', NOW(), NOW(), NULL, 'N')
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    updated_at = NOW(),
    deleted_at = NULL,
    is_deleted = 'N';

INSERT INTO chatbot_messages
    (message_id, session_id, sender_type, content, answerable, next_action, references_json,
     source_worki_question_id, source_ticket_id, created_at, updated_at, deleted_at, is_deleted)
VALUES
    (9001, 9001, 'USER', 'ERP login fails with an authentication error.', TRUE, 'CREATE_WORKI', NULL,
     NULL, NULL, NOW(), NOW(), NULL, 'N')
ON DUPLICATE KEY UPDATE
    content = VALUES(content),
    updated_at = NOW(),
    deleted_at = NULL,
    is_deleted = 'N';

INSERT INTO worki_questions
    (question_id, author_id, source_chatbot_message_id, title, content, status,
     accepted_answer_id, view_count, created_at, updated_at, deleted_at, is_deleted, modified_source)
VALUES
    (9001, 9003, 9001, 'ERP login fails',
     'ERP login fails even when the password is correct.',
     'ANSWERED', NULL, 5, NOW(), NOW(), NULL, 'N', 'USER')
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    content = VALUES(content),
    status = VALUES(status),
    view_count = VALUES(view_count),
    updated_at = NOW(),
    deleted_at = NULL,
    is_deleted = 'N',
    modified_source = 'USER';

INSERT INTO worki_answers
    (answer_id, question_id, author_id, ticket_id, content, official, accepted, accepted_at,
     created_at, updated_at, deleted_at, is_deleted, modified_source)
VALUES
    (9001, 9001, 9002, NULL,
     'Check account lock, password expiration, and network access first. Contact IT Support if it keeps failing.',
     TRUE, TRUE, NOW(), NOW(), NOW(), NULL, 'N', 'ADMIN')
ON DUPLICATE KEY UPDATE
    content = VALUES(content),
    official = VALUES(official),
    accepted = VALUES(accepted),
    accepted_at = VALUES(accepted_at),
    updated_at = NOW(),
    deleted_at = NULL,
    is_deleted = 'N',
    modified_source = 'ADMIN';

UPDATE worki_questions
SET accepted_answer_id = 9001,
    status = 'ANSWERED',
    updated_at = NOW()
WHERE question_id = 9001;

-- ------------------------------------------------------------
-- Ticket data
-- ------------------------------------------------------------
INSERT INTO tickets
    (ticket_id, requester_id, source_chatbot_message_id, title, content, priority, status,
     assigned_department_id, assignee_id, routing_confidence_score, routing_decision,
     completed_at, created_at, updated_at, deleted_at, is_deleted)
VALUES
    (9001, 9003, NULL, 'ERP login error',
     'ERP login fails with an authentication error.',
     'MEDIUM', 'COMPLETED', 9004, 9004, 0.80, 'COMMON_QUEUE',
     NOW(), NOW(), NOW(), NULL, 'N')
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    content = VALUES(content),
    priority = VALUES(priority),
    status = VALUES(status),
    assigned_department_id = VALUES(assigned_department_id),
    assignee_id = VALUES(assignee_id),
    routing_confidence_score = VALUES(routing_confidence_score),
    routing_decision = VALUES(routing_decision),
    completed_at = VALUES(completed_at),
    updated_at = NOW(),
    deleted_at = NULL,
    is_deleted = 'N';

-- ------------------------------------------------------------
-- Knowledge data
-- Completed tickets are treated as knowledge candidates.
-- Approved candidates are copied into knowledge_data.
-- ------------------------------------------------------------
INSERT INTO knowledge_data
    (knowledge_data_id, ticket_id, title, content, department_id, approved_by,
     approved_at, created_at, updated_at, deleted_at, is_deleted, modified_source)
VALUES
    (9001, 9001, 'ERP login troubleshooting',
     'If ERP login fails, check account status, password expiration, and network access first.',
     9004, 9002, NOW(), NOW(), NOW(), NULL, 'N', 'ADMIN')
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    content = VALUES(content),
    department_id = VALUES(department_id),
    approved_by = VALUES(approved_by),
    approved_at = NOW(),
    updated_at = NOW(),
    deleted_at = NULL,
    is_deleted = 'N',
    modified_source = VALUES(modified_source);

-- ------------------------------------------------------------
-- Flash chat policy
-- ------------------------------------------------------------
INSERT INTO flash_chat_policy
    (id, message_ttl_seconds, send_cooldown_seconds, banned_words,
     created_at, updated_at, deleted_at, is_deleted, modified_source)
VALUES
    (1, 600, 0, JSON_ARRAY('blocked-seed-word'), NOW(), NOW(), NULL, 'N', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    message_ttl_seconds = VALUES(message_ttl_seconds),
    send_cooldown_seconds = VALUES(send_cooldown_seconds),
    banned_words = VALUES(banned_words),
    updated_at = NOW(),
    deleted_at = NULL,
    is_deleted = 'N',
    modified_source = 'SYSTEM';

SELECT 'SEED_READY' AS status;
SELECT 'SEED_USER' AS employee_id, 'Test1234!' AS password, 9003 AS user_id;
SELECT 'SEED_TEAM_ADMIN' AS employee_id, 'Test1234!' AS password, 9002 AS user_id;
SELECT 'SEED_ADMIN' AS employee_id, 'Test1234!' AS password, 9001 AS user_id;
