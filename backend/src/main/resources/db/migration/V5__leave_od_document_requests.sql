-- V5: Leave, OD, and Document/Certificate requests
-- All three follow the same mentor→ADMIN routing pattern as Grievance

CREATE TABLE IF NOT EXISTS leave_requests (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id          BIGINT      NOT NULL,
    leave_type          VARCHAR(30) NOT NULL COMMENT 'MEDICAL | PERSONAL | FAMILY | OTHER',
    from_date           DATE        NOT NULL,
    to_date             DATE        NOT NULL,
    period              VARCHAR(100),
    reason              TEXT        NOT NULL,
    attachment_url      VARCHAR(500),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        COMMENT 'PENDING | APPROVED | REJECTED',
    assigned_to_user_id BIGINT,
    assigned_to_role    VARCHAR(20) DEFAULT 'ADMIN',
    resolution_notes    TEXT,
    created_at          DATETIME    DEFAULT CURRENT_TIMESTAMP,
    resolved_at         DATETIME,
    FOREIGN KEY (student_id)          REFERENCES users(id),
    FOREIGN KEY (assigned_to_user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS od_requests (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id          BIGINT       NOT NULL,
    event_name          VARCHAR(200) NOT NULL,
    event_date          DATE         NOT NULL,
    periods             VARCHAR(100)  COMMENT 'Comma-separated period labels',
    location            VARCHAR(200),
    reason              TEXT         NOT NULL,
    proof_url           VARCHAR(500),
    status              VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    assigned_to_user_id BIGINT,
    assigned_to_role    VARCHAR(20)  DEFAULT 'ADMIN',
    resolution_notes    TEXT,
    created_at          DATETIME     DEFAULT CURRENT_TIMESTAMP,
    resolved_at         DATETIME,
    FOREIGN KEY (student_id)          REFERENCES users(id),
    FOREIGN KEY (assigned_to_user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS document_requests (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id          BIGINT      NOT NULL,
    document_type       VARCHAR(50) NOT NULL
        COMMENT 'BONAFIDE | TRANSCRIPT | NOC | CONDUCT_CERT | MIGRATION_CERT',
    purpose             TEXT        NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        COMMENT 'PENDING | PROCESSING | READY | REJECTED',
    assigned_to_user_id BIGINT,
    assigned_to_role    VARCHAR(20) DEFAULT 'ADMIN',
    resolution_notes    TEXT,
    created_at          DATETIME    DEFAULT CURRENT_TIMESTAMP,
    resolved_at         DATETIME,
    FOREIGN KEY (student_id)          REFERENCES users(id),
    FOREIGN KEY (assigned_to_user_id) REFERENCES users(id)
);
