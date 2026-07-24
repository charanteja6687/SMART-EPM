CREATE DATABASE IF NOT EXISTS smart_epm_db;
USE smart_epm_db;

-- ==========================================================
-- Table: employees
-- ==========================================================
CREATE TABLE IF NOT EXISTS employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),
    department VARCHAR(100),
    designation VARCHAR(100),
    salary DOUBLE,
    date_of_joining DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at DATETIME NULL,           -- soft delete marker; NULL = active
    created_at DATETIME,
    updated_at DATETIME
);

-- ==========================================================
-- Table: users (Authentication)
-- No "employee_id" is ever supplied by the person registering — the backend
-- auto-links (or auto-creates) the matching Employee row by email.
-- ==========================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN','EMPLOYEE') NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    employee_id BIGINT NULL,            -- internal FK only, never entered manually by a user
    reset_otp_code VARCHAR(6) NULL,      -- forgot-password OTP, NULL when no reset in progress
    reset_otp_expiry DATETIME NULL,      -- OTP valid for 5 minutes from generation
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT fk_users_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE SET NULL
);

-- ==========================================================
-- Table: projects
-- ==========================================================
CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(2000),
    status ENUM('ACTIVE','COMPLETED','ON_HOLD','CANCELLED') NOT NULL,
    priority ENUM('HIGH','MEDIUM','LOW') NOT NULL,
    start_date DATE,
    deadline DATE,
    deleted_at DATETIME NULL,
    created_at DATETIME,
    updated_at DATETIME
);

-- ==========================================================
-- Table: project_employee (Many-to-Many) — determines which employees
-- can see which projects (role-based access filters on this table).
-- ==========================================================
CREATE TABLE IF NOT EXISTS project_employee (
    project_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    PRIMARY KEY (project_id, employee_id),
    CONSTRAINT fk_pe_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_pe_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- ==========================================================
-- Table: tasks
-- ==========================================================
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    status ENUM('TODO','IN_PROGRESS','COMPLETED','BLOCKED') NOT NULL,
    priority ENUM('HIGH','MEDIUM','LOW') NOT NULL,
    progress INT NOT NULL DEFAULT 0,
    due_date DATE,
    remarks VARCHAR(2000),
    project_id BIGINT NOT NULL,
    employee_id BIGINT NULL,            -- assignee; determines what an EMPLOYEE user can see
    deleted_at DATETIME NULL,
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT fk_tasks_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE SET NULL
);

-- ==========================================================
-- Table: notifications
-- ==========================================================
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    message VARCHAR(300) NOT NULL,
    type VARCHAR(40),
    related_entity_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME,
    CONSTRAINT fk_notifications_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- ==========================================================
-- Table: activity_logs (audit trail — write-once, never updated)
-- ==========================================================
CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_username VARCHAR(100) NOT NULL,
    action VARCHAR(30) NOT NULL,
    entity_type VARCHAR(30) NOT NULL,
    entity_id BIGINT,
    description VARCHAR(500) NOT NULL,
    timestamp DATETIME NOT NULL
);

-- ==========================================================
-- Seed data (optional — DataSeeder.java also creates the admin
-- user automatically on first backend startup)
-- ==========================================================

-- Default admin user (password: Admin@123, BCrypt-encoded)
INSERT INTO users (username, email, password, role, enabled, created_at, updated_at)
SELECT 'admin', 'admin@smartepm.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5L2yv4rW5Ug0j5v2Ni9c3Q7wA3g0e', 'ADMIN', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

-- Sample employees
INSERT INTO employees (full_name, email, phone, department, designation, salary, date_of_joining, active, created_at, updated_at)
VALUES
 ('Ravi Kumar', 'ravi.kumar@smartepm.com', '9876543210', 'Engineering', 'Software Engineer', 55000, '2023-01-15', TRUE, NOW(), NOW()),
 ('Anita Sharma', 'anita.sharma@smartepm.com', '9876543211', 'Engineering', 'Senior Software Engineer', 75000, '2021-06-01', TRUE, NOW(), NOW()),
 ('Suresh Reddy', 'suresh.reddy@smartepm.com', '9876543212', 'QA', 'QA Engineer', 45000, '2022-03-10', TRUE, NOW(), NOW());

-- Sample project, assigned to Ravi Kumar (employee id 1)
INSERT INTO projects (name, description, status, priority, start_date, deadline, created_at, updated_at)
VALUES ('Smart EPM Rollout', 'Internal rollout of the employee & project management platform', 'ACTIVE', 'HIGH', '2026-07-01', '2026-09-30', NOW(), NOW());

INSERT INTO project_employee (project_id, employee_id) VALUES (1, 1);

-- Sample task, assigned to Ravi Kumar (employee id 1)
INSERT INTO tasks (title, description, status, priority, progress, due_date, remarks, project_id, employee_id, created_at, updated_at)
VALUES ('Setup CI/CD pipeline', 'Configure GitHub Actions for backend build & deploy', 'IN_PROGRESS', 'HIGH', 40, '2026-08-05', 'In progress, blocked on secrets config', 1, 1, NOW(), NOW());
