package com.agentx.campus.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "campus_documents")
public class CampusDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 50)
    private String category; // REGULATION, SYLLABUS, POLICY, EXAM_RULES, CAMPUS_GUIDE

    @Column(length = 1000)
    private String description;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    private String fileLocation;
    private String department = "All Departments";
    private String documentType = "PDF";
    private String version = "1.0";
    private String uploadedBy = "Admin";
    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    public CampusDocument() {}

    public CampusDocument(String title, String category, String description, String content,
                          String department, String documentType, String version, String uploadedBy) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.content = content;
        this.department = department;
        this.documentType = documentType;
        this.version = version;
        this.uploadedBy = uploadedBy;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getFileLocation() { return fileLocation; }
    public void setFileLocation(String fileLocation) { this.fileLocation = fileLocation; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
