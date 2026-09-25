-- V3: Add complaint/grievance routing fields
ALTER TABLE grievances
    ADD COLUMN assigned_to_user_id BIGINT NULL,
    ADD COLUMN assigned_to_role VARCHAR(20) DEFAULT 'ADMIN',
    ADD COLUMN assigned_to VARCHAR(100) DEFAULT 'Administration',
    ADD COLUMN routed_to VARCHAR(100) DEFAULT 'ADMIN',
    ADD COLUMN resolution_notes VARCHAR(1000) NULL,
    ADD CONSTRAINT fk_grievance_assigned_to FOREIGN KEY (assigned_to_user_id) REFERENCES users(id) ON DELETE SET NULL;
