package com.agentx.campus.model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agent_task_logs")
public class AgentTaskLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String agentType;
    private String selectedAgents;
    private String toolsUsed;
    private int sourcesCount = 0;
    private String userQuery;
    @Column(length = 1000)
    private String actionSummary;
    private String executionStatus;
    private long latencyMs;
    private LocalDateTime createdAt = LocalDateTime.now();

    public AgentTaskLog() {}

    public Long getId() { return id; }
    @JsonIgnore
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getAgentType() { return agentType; }
    public void setAgentType(String agentType) { this.agentType = agentType; }
    public String getSelectedAgents() { return selectedAgents; }
    public void setSelectedAgents(String selectedAgents) { this.selectedAgents = selectedAgents; }
    public String getToolsUsed() { return toolsUsed; }
    public void setToolsUsed(String toolsUsed) { this.toolsUsed = toolsUsed; }
    public int getSourcesCount() { return sourcesCount; }
    public void setSourcesCount(int sourcesCount) { this.sourcesCount = sourcesCount; }
    public String getUserQuery() { return userQuery; }
    public void setUserQuery(String userQuery) { this.userQuery = userQuery; }
    public String getActionSummary() { return actionSummary; }
    public void setActionSummary(String actionSummary) { this.actionSummary = actionSummary; }
    public String getExecutionStatus() { return executionStatus; }
    public void setExecutionStatus(String executionStatus) { this.executionStatus = executionStatus; }
    public long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(long latencyMs) { this.latencyMs = latencyMs; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
