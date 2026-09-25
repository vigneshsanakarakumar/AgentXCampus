package com.agentx.campus.dto;

import java.util.ArrayList;
import java.util.List;

public class ExecutionPlanDto {
    private String taskId;
    private String intent;
    private String status; // PENDING, RUNNING, COMPLETED, FAILED, RETRYING, REQUIRES_APPROVAL
    private List<ExecutionStepDto> steps = new ArrayList<>();
    private VerificationResultDto verification;
    private long totalLatencyMs;

    public ExecutionPlanDto() {}

    public ExecutionPlanDto(String taskId, String intent, String status) {
        this.taskId = taskId;
        this.intent = intent;
        this.status = status;
    }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<ExecutionStepDto> getSteps() { return steps; }
    public void setSteps(List<ExecutionStepDto> steps) { this.steps = steps; }

    public VerificationResultDto getVerification() { return verification; }
    public void setVerification(VerificationResultDto verification) { this.verification = verification; }

    public long getTotalLatencyMs() { return totalLatencyMs; }
    public void setTotalLatencyMs(long totalLatencyMs) { this.totalLatencyMs = totalLatencyMs; }

    public void addStep(ExecutionStepDto step) {
        this.steps.add(step);
    }
}
