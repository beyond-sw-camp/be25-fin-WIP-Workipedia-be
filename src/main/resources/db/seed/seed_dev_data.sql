-- ============================================================
-- 개발/테스트용 시드 데이터 (departments + users)
-- 주의: Flyway 마이그레이션이 아니라 수동 실행용 스크립트다.
--       (db/migration 이 아니라 db/seed 에 두는 이유 = Flyway 가 자동 실행하지 않도록)
-- 실행: mariadb -u wip -p workipedia < src/main/resources/db/seed/seed_dev_data.sql
-- 로그인 비밀번호(모든 시드 유저 공통): Test1234!  (BCrypt 해시로 저장)
-- 재실행 가능하도록 기존 시드 행을 먼저 지운다.
-- ============================================================

-- FK 때문에 users -> departments 순서로 지우고, departments -> users 순서로 넣는다.
DELETE FROM users WHERE employee_id IN ('EMP0001', 'EMP0002', 'EMP0003', 'EMP0004', 'EMP0005');
DELETE FROM departments WHERE department_name IN ('인사팀', '총무팀', 'IT지원팀', '재무팀', '법무팀');

-- ------------------------------------------------------------
-- 1) 부서
-- ------------------------------------------------------------
INSERT INTO departments (department_name, is_deleted) VALUES
    ('인사팀',    'N'),
    ('총무팀',    'N'),
    ('IT지원팀',  'N'),
    ('재무팀',    'N'),
    ('법무팀',    'N');

-- ------------------------------------------------------------
-- 2) 사용자
--    department_id 는 auto_increment 값에 의존하지 않도록 부서명으로 조회해서 넣는다.
--    password = BCrypt('Test1234!')
-- ------------------------------------------------------------
INSERT INTO users (department_id, employee_id, email, password, nickname, role, status) VALUES
    ((SELECT department_id FROM departments WHERE department_name = 'IT지원팀'),
     'EMP0001', 'admin@workipedia.com',
     '$2a$10$q7AJJ7UKfbkljvBlGDzTj.apD0pB2N3z8eGuut1BjR98y5k.pQh4O',
     '시스템관리자', 'SYSTEM_ADMIN', 'ACTIVE'),

    ((SELECT department_id FROM departments WHERE department_name = '인사팀'),
     'EMP0002', 'hr.lead@workipedia.com',
     '$2a$10$q7AJJ7UKfbkljvBlGDzTj.apD0pB2N3z8eGuut1BjR98y5k.pQh4O',
     '인사팀장', 'TEAM_ADMIN', 'ACTIVE'),

    ((SELECT department_id FROM departments WHERE department_name = '인사팀'),
     'EMP0003', 'user1@workipedia.com',
     '$2a$10$q7AJJ7UKfbkljvBlGDzTj.apD0pB2N3z8eGuut1BjR98y5k.pQh4O',
     '김사원', 'USER', 'ACTIVE'),

    ((SELECT department_id FROM departments WHERE department_name = '총무팀'),
     'EMP0004', 'user2@workipedia.com',
     '$2a$10$q7AJJ7UKfbkljvBlGDzTj.apD0pB2N3z8eGuut1BjR98y5k.pQh4O',
     '이주임', 'USER', 'ACTIVE'),

    ((SELECT department_id FROM departments WHERE department_name = '재무팀'),
     'EMP0005', 'user3@workipedia.com',
     '$2a$10$q7AJJ7UKfbkljvBlGDzTj.apD0pB2N3z8eGuut1BjR98y5k.pQh4O',
     '박대리', 'USER', 'ACTIVE');
