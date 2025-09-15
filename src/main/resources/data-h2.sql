-- data-h2.sql (예시 템플릿)
SET REFERENTIAL_INTEGRITY FALSE;

-- departments 예시 데이터
INSERT INTO departments (id, name, description, established_date) VALUES (1, '인사부', '인사팀', '2020-01-01');
INSERT INTO departments (id, name, description, established_date) VALUES (2, '개발부', '개발팀', '2021-06-01');

-- files 예시
INSERT INTO files (id, file_name, content_type, size) VALUES (1, 'profile1.jpg', 'image/jpeg', 1024);

-- employees 예시 (DO 블록으로 생성하던 항목은 여기서 개별 INSERT로 변환)
INSERT INTO employees (id, name, email, employee_number, department_id, position, hire_date, status, profile_image_id)
VALUES (1, '테스트1', 'test1@ex.com', 'EMP00001', 1, 'ENGINEER', '2022-01-01', 'ACTIVE', 1);

-- change_logs, change_log_diffs, backups 등도 마찬가지로 INSERT 나열
-- ...

-- identity/sequence 초기값 맞추기 (필요 시)
ALTER TABLE departments ALTER COLUMN id RESTART WITH 100;
ALTER TABLE employees ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE files ALTER COLUMN id RESTART WITH 100;

SET REFERENTIAL_INTEGRITY TRUE;
