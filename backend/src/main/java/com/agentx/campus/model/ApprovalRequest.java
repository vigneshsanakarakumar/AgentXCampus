package com.agentx.campus.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "approval_requests")
public class ApprovalRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String description;

    private String agentName;
    private String riskLevel; // LOW, MEDIUM, HIGH
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, MODIFIED

    private String actionType; // e.g. TIMETABLE_RESCHEDULE, GRADE_REVISION, NOTICE_BROADCAST
    private Long targetEntityId;

    @Column(length = 1000)
    private String mutationPayload; // JSON string with mutation parameters

    private LocalDateTime requestedAt = LocalDateTime.now();
    private LocalDateTime decidedAt;
    private String decidedBy;

    public ApprovalRequest() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public Long getTargetEntityId() { return targetEntityId; }
    public void setTargetEntityId(Long targetEntityId) { this.targetEntityId = targetEntityId; }

    public String getMutationPayload() { return mutationPayload; }
    public void setMutationPayload(String mutationPayload) { this.mutationPayload = mutationPayload; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }

    public String getDecidedBy() { return decidedBy; }
    public void setDecidedBy(String decidedBy) { this.decidedBy = decidedBy; }
}
