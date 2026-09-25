-- V9: Notification explainability — adds matched_because column to existing notifications table

ALTER TABLE notifications
    ADD COLUMN matched_because VARCHAR(255) NULL
        COMMENT 'Targeting criteria that caused this notification (e.g. Section: CSE Sec C)';
