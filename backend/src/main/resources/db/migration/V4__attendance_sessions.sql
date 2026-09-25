-- V4: Session-based attendance marking
-- AttendanceSession: one row per class session (date + period + subject)
-- AttendanceEntry:   one row per student per session with PRESENT/ABSENT/LEAVE/OD/LATE/MEDICAL_LEAVE

CREATE TABLE IF NOT EXISTS attendance_sessions (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_id  BIGINT       NOT NULL,
    faculty_id  BIGINT       NOT NULL,
    subject_code VARCHAR(30) NOT NULL,
    subject_name VARCHAR(150) NOT NULL,
    session_date DATE         NOT NULL,
    period       VARCHAR(50)  NOT NULL COMMENT 'e.g. Period 1 / 09:00-10:00',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (section_id) REFERENCES faculty_mentor_sections(id),
    FOREIGN KEY (faculty_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS attendance_entries (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id  BIGINT      NOT NULL,
    student_id  BIGINT      NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'PRESENT'
        COMMENT 'PRESENT | ABSENT | LEAVE | OD | LATE | MEDICAL_LEAVE',
    remarks     VARCHAR(255),
    FOREIGN KEY (session_id) REFERENCES attendance_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id),
    UNIQUE KEY uq_session_student (session_id, student_id)
);
