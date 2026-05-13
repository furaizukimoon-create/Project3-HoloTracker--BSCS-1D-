
-- Step 1: Create and select the database
CREATE DATABASE IF NOT EXISTS grade_tracker_db;
USE grade_tracker_db;

-- Step 2: Drop tables if re-running (order matters due to FK)
DROP TABLE IF EXISTS grades;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS users;

-- ============================================================
-- TABLE: users
-- Stores admin/teacher login credentials
-- ============================================================
CREATE TABLE users (
    user_id    INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,  -- SHA-256 hashed
    full_name  VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE: students
-- Stores each student's personal information
-- ============================================================
CREATE TABLE students (
    student_id     INT AUTO_INCREMENT PRIMARY KEY,
    student_number VARCHAR(20)  NOT NULL UNIQUE,
    first_name     VARCHAR(50)  NOT NULL,
    last_name      VARCHAR(50)  NOT NULL,
    year_level     INT          NOT NULL CHECK (year_level BETWEEN 1 AND 4),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE: subjects
-- Stores available subjects
-- ============================================================
CREATE TABLE subjects (
    subject_id   INT AUTO_INCREMENT PRIMARY KEY,
    subject_code VARCHAR(20)  NOT NULL UNIQUE,
    subject_name VARCHAR(100) NOT NULL
);

-- ============================================================
-- TABLE: grades
-- Links students to subjects with a grade value
-- Central relational table (has 2 foreign keys)
-- ============================================================
CREATE TABLE grades (
    grade_id      INT AUTO_INCREMENT PRIMARY KEY,
    student_id    INT            NOT NULL,
    subject_id    INT            NOT NULL,
    grade         DECIMAL(5,2)   NOT NULL CHECK (grade BETWEEN 0 AND 100),
    date_recorded TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE,
    UNIQUE KEY unique_student_subject (student_id, subject_id)
);

-- ============================================================
-- SEED DATA: Default admin user
-- Username: admin | Password: admin123 (SHA-256 hashed)
-- ============================================================
INSERT INTO users (username, password, full_name) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Administrator');

-- ============================================================
-- SEED DATA: Sample subjects
-- ============================================================
INSERT INTO subjects (subject_code, subject_name) VALUES
('CC101', 'Introduction to Computing'),
('CC102', 'Computer Programming 1'),
('CC103', 'Computer Programming 2'),
('IM101', 'Information Management'),
('MM101', 'Mathematics in the Modern World'),
('GE101', 'Understanding the Self');

-- ============================================================
-- SEED DATA: Sample students
-- ============================================================
INSERT INTO students (student_number, first_name, last_name, year_level) VALUES
('2024-00001', 'Sakura',  'Miko',    1),
('2024-00002', 'Hoshimachi', 'Suisei', 1),
('2024-00003', 'Tokino',  'Sora',    2),
('2024-00004', 'Shirakami', 'Fubuki', 2),
('2024-00005', 'Minato',  'Aqua',    1);

-- ============================================================
-- SEED DATA: Sample grades
-- ============================================================
INSERT INTO grades (student_id, subject_id, grade) VALUES
(1, 1, 92.50), (1, 2, 88.00), (1, 3, 79.50), (1, 4, 95.00),
(2, 1, 98.00), (2, 2, 97.50), (2, 3, 96.00), (2, 4, 99.00),
(3, 1, 85.00), (3, 2, 72.00), (3, 3, 68.50),
(4, 1, 74.00), (4, 2, 73.50), (4, 3, 71.00),
(5, 1, 55.00), (5, 2, 60.00);

-- ============================================================
-- VERIFICATION QUERY: Test the 3-table JOIN
-- Run this to confirm everything is set up correctly
-- ============================================================
SELECT
    CONCAT(s.first_name, ' ', s.last_name) AS student_name,
    s.student_number,
    sub.subject_code,
    sub.subject_name,
    g.grade,
    g.date_recorded
FROM grades g
JOIN students s   ON g.student_id  = s.student_id
JOIN subjects sub ON g.subject_id  = sub.subject_id
ORDER BY s.last_name, sub.subject_code;

SELECT 'Database setup complete! Login with: admin / admin123' AS status;
