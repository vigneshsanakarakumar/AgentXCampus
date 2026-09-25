-- V2: Add Mentor-to-Section Mapping and Staff Login Requests Tables

CREATE TABLE IF NOT EXISTS faculty_mentor_sections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    faculty_profile_id BIGINT NOT NULL,
    department VARCHAR(50) NOT NULL,
    section VARCHAR(10) NOT NULL,
    semester INT NOT NULL,
    academic_year VARCHAR(20) DEFAULT '2025-2026',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mentor_faculty FOREIGN KEY (faculty_profile_id) REFERENCES faculty_profiles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS staff_login_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    department VARCHAR(50) NOT NULL,
    designation VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    invite_token VARCHAR(100) UNIQUE,
    token_expiry DATETIME,
    rejection_reason VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    approved_at DATETIME
);
