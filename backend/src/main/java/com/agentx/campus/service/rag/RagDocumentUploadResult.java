package com.agentx.campus.service.rag;

public class RagDocumentUploadResult {
    private String documentId;
    private String documentName;
    private String status; // INDEXED, PARSING, FAILED
    private String message;
    private int chunksCount;

    public RagDocumentUploadResult() {}

    public RagDocumentUploadResult(String documentId, String documentName, String status, String message, int chunksCount) {
        this.documentId = documentId;
        this.documentName = documentName;
        this.status = status;
        this.message = message;
        this.chunksCount = chunksCount;
    }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getChunksCount() { return chunksCount; }
    public void setChunksCount(int chunksCount) { this.chunksCount = chunksCount; }
}
