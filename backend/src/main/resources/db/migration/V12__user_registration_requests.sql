-- V12: User Registration Requests for HOD Approval Workflow

ALTER TABLE faculty_profiles ADD COLUMN specialization VARCHAR(255) NULL;

CREATE TABLE IF NOT EXISTS user_registration_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    department VARCHAR(100) NOT NULL,
    roll_number VARCHAR(50),
    section VARCHAR(10),
    year INT,
    semester INT,
    designation VARCHAR(100),
    specialization VARCHAR(255),
    cabin_number VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    hod_user_id BIGINT,
    rejection_reason TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    resolved_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (hod_user_id) REFERENCES users(id) ON DELETE SET NULL
);
