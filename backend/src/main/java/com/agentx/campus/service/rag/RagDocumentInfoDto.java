package com.agentx.campus.service.rag;

public class RagDocumentInfoDto {
    private String id;
    private String name;
    private String category;
    private String department;
    private String version;
    private String status; // INDEXED, PARSING, ERROR
    private int chunkCount;
    private String source; // RAGFLOW or EMBEDDED
    private String updatedAt;

    public RagDocumentInfoDto() {}

    public RagDocumentInfoDto(String id, String name, String category, String department,
                             String version, String status, int chunkCount, String source, String updatedAt) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.department = department;
        this.version = version;
        this.status = status;
        this.chunkCount = chunkCount;
        this.source = source;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getChunkCount() { return chunkCount; }
    public void setChunkCount(int chunkCount) { this.chunkCount = chunkCount; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
