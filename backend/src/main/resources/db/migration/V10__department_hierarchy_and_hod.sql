-- V10: Department Hierarchy — HOD -> Faculty -> Student
-- Tables for HOD Profiles, Faculty Leave/Permission Requests, and Scoped Routing Columns

CREATE TABLE IF NOT EXISTS hod_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL UNIQUE,
    cabin_number VARCHAR(50),
    contact_number VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS faculty_leave_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    faculty_id BIGINT NOT NULL,
    department VARCHAR(100) NOT NULL,
    leave_type VARCHAR(50) NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    substitute_faculty_name VARCHAR(100),
    reason TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    hod_user_id BIGINT,
    resolution_notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    resolved_at DATETIME,
    FOREIGN KEY (faculty_id) REFERENCES users(id),
    FOREIGN KEY (hod_user_id) REFERENCES users(id)
);

ALTER TABLE announcements ADD COLUMN target_department VARCHAR(100) NULL;
ALTER TABLE announcements ADD COLUMN target_section VARCHAR(20) NULL;

ALTER TABLE campus_events ADD COLUMN target_department VARCHAR(100) NULL;
ALTER TABLE campus_events ADD COLUMN target_section VARCHAR(20) NULL;
ALTER TABLE campus_events ADD COLUMN target_audience VARCHAR(50) DEFAULT 'ALL';
