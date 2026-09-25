-- V8: Opportunities / Placement board

CREATE TABLE IF NOT EXISTS opportunities (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    title             VARCHAR(200) NOT NULL,
    description       TEXT         NOT NULL,
    type              VARCHAR(50)  NOT NULL
        COMMENT 'INTERNSHIP | HACKATHON | WORKSHOP | CERTIFICATION | PLACEMENT_DRIVE | COMPETITION',
    department_filter VARCHAR(100) COMMENT 'NULL = all departments',
    year_filter       INT          COMMENT 'NULL = all years',
    eligibility_text  TEXT,
    external_link     VARCHAR(500),
    deadline          DATE,
    posted_by         BIGINT,
    created_at        DATETIME     DEFAULT CURRENT_TIMESTAMP,
    is_active         BOOLEAN      NOT NULL DEFAULT TRUE,
    FOREIGN KEY (posted_by) REFERENCES users(id)
);

INSERT IGNORE INTO opportunities
    (id, title, description, type, department_filter, year_filter, eligibility_text, external_link, deadline, is_active)
VALUES
(1, 'Google Summer of Code 2026',
 'Contribute to open-source projects under Google mentorship. Work on real-world coding challenges over the summer.',
 'INTERNSHIP', NULL, 3,
 'CGPA >= 7.0, strong programming skills in any open-source language.',
 'https://summerofcode.withgoogle.com', '2026-11-15', TRUE),

(2, 'IEEE National Hackathon 2026',
 'National-level hackathon for engineering students. Teams of 3–4. Problem statements across AI, IoT, and Web3.',
 'HACKATHON', NULL, NULL,
 'All year students eligible. Team registration required.',
 'https://ieee.org/hackathon2026', '2026-10-30', TRUE),

(3, 'AWS Cloud Practitioner Certification',
 'Free certification exam voucher for students enrolled in cloud computing courses.',
 'CERTIFICATION', 'Computer Science & Engineering', NULL,
 'Must be enrolled in CS306 Cloud Computing Laboratory.',
 'https://aws.amazon.com/training', '2026-12-01', TRUE);
