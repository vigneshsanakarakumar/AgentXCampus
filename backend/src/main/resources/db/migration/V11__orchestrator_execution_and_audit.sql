-- V11: Agent Orchestrator Execution State, Step Trace & Enterprise Audit Logging

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action_type VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    actor_username VARCHAR(100) NOT NULL,
    actor_role VARCHAR(50),
    details TEXT,
    status VARCHAR(50) DEFAULT 'SUCCESS',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS agent_execution_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT,
    intent VARCHAR(100) NOT NULL,
    user_query TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    total_steps INT DEFAULT 0,
    completed_steps INT DEFAULT 0,
    plan_json LONGTEXT,
    verification_status VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS agent_execution_steps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id VARCHAR(64) NOT NULL,
    step_number INT NOT NULL,
    agent_name VARCHAR(100) NOT NULL,
    tool_name VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    input_summary TEXT,
    output_summary TEXT,
    error_message TEXT,
    retry_count INT DEFAULT 0,
    start_time DATETIME,
    end_time DATETIME,
    FOREIGN KEY (task_id) REFERENCES agent_execution_plans(task_id) ON DELETE CASCADE
);

CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_username);
CREATE INDEX idx_audit_logs_action ON audit_logs(action_type);
CREATE INDEX idx_agent_plans_task_id ON agent_execution_plans(task_id);
CREATE INDEX idx_agent_steps_task_id ON agent_execution_steps(task_id);
