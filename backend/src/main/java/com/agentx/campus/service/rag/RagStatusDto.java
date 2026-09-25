package com.agentx.campus.service.rag;

public class RagStatusDto {
    private String activeProvider;
    private boolean ragflowEnabled;
    private boolean ragflowHealthy;
    private String ragflowUrl;
    private String datasetId;
    private int totalDocuments;
    private int totalChunks;
    private String statusMessage;

    public RagStatusDto() {}

    public RagStatusDto(String activeProvider, boolean ragflowEnabled, boolean ragflowHealthy,
                        String ragflowUrl, String datasetId, int totalDocuments, int totalChunks,
                        String statusMessage) {
        this.activeProvider = activeProvider;
        this.ragflowEnabled = ragflowEnabled;
        this.ragflowHealthy = ragflowHealthy;
        this.ragflowUrl = ragflowUrl;
        this.datasetId = datasetId;
        this.totalDocuments = totalDocuments;
        this.totalChunks = totalChunks;
        this.statusMessage = statusMessage;
    }

    public String getActiveProvider() { return activeProvider; }
    public void setActiveProvider(String activeProvider) { this.activeProvider = activeProvider; }

    public boolean isRagflowEnabled() { return ragflowEnabled; }
    public void setRagflowEnabled(boolean ragflowEnabled) { this.ragflowEnabled = ragflowEnabled; }

    public boolean isRagflowHealthy() { return ragflowHealthy; }
    public void setRagflowHealthy(boolean ragflowHealthy) { this.ragflowHealthy = ragflowHealthy; }

    public String getRagflowUrl() { return ragflowUrl; }
    public void setRagflowUrl(String ragflowUrl) { this.ragflowUrl = ragflowUrl; }

    public String getDatasetId() { return datasetId; }
    public void setDatasetId(String datasetId) { this.datasetId = datasetId; }

    public int getTotalDocuments() { return totalDocuments; }
    public void setTotalDocuments(int totalDocuments) { this.totalDocuments = totalDocuments; }

    public int getTotalChunks() { return totalChunks; }
    public void setTotalChunks(int totalChunks) { this.totalChunks = totalChunks; }

    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
}
