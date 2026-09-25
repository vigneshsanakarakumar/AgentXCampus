package com.agentx.campus.dto;

public class ApprovalDecisionRequest {
    private String decision; // APPROVED or REJECTED
    public ApprovalDecisionRequest() {}
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
}
