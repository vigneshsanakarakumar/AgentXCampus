package com.agentx.campus.dto;

import java.util.List;

public class AgentChatResponse {
    private String answer;
    private String agentType;
    private List<String> steps;
    private Object actionData;
    private long latencyMs;
    private ExecutionPlanDto executionPlan;
    private VerificationResultDto verification;

    public AgentChatResponse(String answer, String agentType, List<String> steps, Object actionData, long latencyMs) {
        this.answer = answer;
        this.agentType = agentType;
        this.steps = steps;
        this.actionData = actionData;
        this.latencyMs = latencyMs;
    }

    public AgentChatResponse(String answer, String agentType, List<String> steps, Object actionData, long latencyMs,
                             ExecutionPlanDto executionPlan, VerificationResultDto verification) {
        this.answer = answer;
        this.agentType = agentType;
        this.steps = steps;
        this.actionData = actionData;
        this.latencyMs = latencyMs;
        this.executionPlan = executionPlan;
        this.verification = verification;
    }

    public String getAnswer() { return answer; }
    public String getMessage() { return answer; }
    public String getAgentType() { return agentType; }
    public List<String> getSteps() { return steps; }
    public Object getActionData() { return actionData; }
    public long getLatencyMs() { return latencyMs; }

    public ExecutionPlanDto getExecutionPlan() { return executionPlan; }
    public void setExecutionPlan(ExecutionPlanDto executionPlan) { this.executionPlan = executionPlan; }

    public VerificationResultDto getVerification() { return verification; }
    public void setVerification(VerificationResultDto verification) { this.verification = verification; }
}
