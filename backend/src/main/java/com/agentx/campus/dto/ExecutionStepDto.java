package com.agentx.campus.dto;

public class ExecutionStepDto {
    private int step;
    private String agent;
    private String tool;
    private String status; // PENDING, RUNNING, COMPLETED, FAILED, RETRYING, REQUIRES_APPROVAL
    private String inputSummary;
    private String outputSummary;
    private String error;
    private int retryCount;
    private long latencyMs;

    public ExecutionStepDto() {}

    public ExecutionStepDto(int step, String agent, String tool, String status) {
        this.step = step;
        this.agent = agent;
        this.tool = tool;
        this.status = status;
    }

    public int getStep() { return step; }
    public void setStep(int step) { this.step = step; }

    public String getAgent() { return agent; }
    public void setAgent(String agent) { this.agent = agent; }

    public String getTool() { return tool; }
    public void setTool(String tool) { this.tool = tool; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getInputSummary() { return inputSummary; }
    public void setInputSummary(String inputSummary) { this.inputSummary = inputSummary; }

    public String getOutputSummary() { return outputSummary; }
    public void setOutputSummary(String outputSummary) { this.outputSummary = outputSummary; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(long latencyMs) { this.latencyMs = latencyMs; }
}
