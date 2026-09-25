-- V7: Exam scheduling with conflict detection support

CREATE TABLE IF NOT EXISTS exam_schedules (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_code VARCHAR(30)  NOT NULL,
    subject_name VARCHAR(150) NOT NULL,
    department   VARCHAR(100) NOT NULL,
    section      VARCHAR(10)  NOT NULL,
    semester     INT          NOT NULL DEFAULT 1,
    exam_date    DATE         NOT NULL,
    start_time   VARCHAR(20)  NOT NULL,
    end_time     VARCHAR(20)  NOT NULL,
    room         VARCHAR(50)  NOT NULL,
    exam_type    VARCHAR(30)  NOT NULL DEFAULT 'INTERNAL'
        COMMENT 'INTERNAL | EXTERNAL | PLACEMENT_TEST',
    created_by   BIGINT,
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);
