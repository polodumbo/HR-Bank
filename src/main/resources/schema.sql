DROP TABLE IF EXISTS change_log_diffs CASCADE;
DROP TABLE IF EXISTS change_logs CASCADE;
DROP TABLE IF EXISTS backups CASCADE;
DROP TABLE IF EXISTS employees CASCADE;
DROP TABLE IF EXISTS files CASCADE;
DROP TABLE IF EXISTS departments CASCADE;

/* 부서 departments */
CREATE TABLE departments
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(50) NOT NULL UNIQUE,
    description      VARCHAR(255) NOT NULL,
    established_date DATE NOT NULL
);

/* 파일 files */
CREATE TABLE files
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name    VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size         BIGINT NOT NULL
);

/* 직원 employees */
CREATE TABLE employees
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(50) NOT NULL,
    email            VARCHAR(100) NOT NULL UNIQUE,
    employee_number  VARCHAR(50) NOT NULL UNIQUE,
    department_id    BIGINT NOT NULL,
    position         VARCHAR(50) NOT NULL,
    hire_date        DATE NOT NULL,
    status           VARCHAR(50) NOT NULL,
    profile_image_id BIGINT,
    CONSTRAINT chk_employees_status CHECK (status IN ('ACTIVE', 'ON_LEAVE', 'RESIGNED')),
    CONSTRAINT employees_departments_id_fk FOREIGN KEY (department_id) REFERENCES departments,
    CONSTRAINT employees_files_id_fk FOREIGN KEY (profile_image_id) REFERENCES files ON DELETE SET NULL
);

/* 직원 정보 수정 이력 change_logs */
CREATE TABLE change_logs
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    type            VARCHAR(50) NOT NULL,
    employee_number VARCHAR(50) NOT NULL,
    memo            VARCHAR(255),
    ip_address      VARCHAR(50) NOT NULL,
    at              TIMESTAMP NOT NULL,
    CONSTRAINT chk_change_logs_type CHECK (type IN ('CREATED', 'UPDATED', 'DELETED'))
);

/* 직원 정보 수정 상세 이력 change_log_diffs */
CREATE TABLE change_log_diffs
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    log_id        BIGINT NOT NULL,
    property_name VARCHAR(50) NOT NULL,
    before        VARCHAR(100),
    after         VARCHAR(100),
    CONSTRAINT change_log_diffs_change_logs_id_fk FOREIGN KEY (log_id) REFERENCES change_logs(id) ON DELETE CASCADE
);

/* 데이터 백업 backups */
CREATE TABLE backups
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    worker     VARCHAR(50) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    ended_at   TIMESTAMP,
    status     VARCHAR(50) NOT NULL,
    file_id    BIGINT,
    CONSTRAINT backups_files_id_fk FOREIGN KEY (file_id) REFERENCES files
);
