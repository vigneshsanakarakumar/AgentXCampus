package com.agentx.campus.dto;

public class DocumentRequest {
    private String title;
    private String category;
    private String description;
    private String content;
    private String department;
    private String documentType;
    private String version;

    public DocumentRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
