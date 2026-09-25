package com.agentx.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_requests")
public class DocumentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private User student;

    /** BONAFIDE | TRANSCRIPT | NOC | CONDUCT_CERT | MIGRATION_CERT */
    @Column(nullable = false, length = 50)
    private String documentType;

    @Column(nullable = false, length = 2000)
    private String purpose;

    /** PENDING | PROCESSING | READY | REJECTED */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    @JsonIgnore
    private User assignedToUser;

    @Column(name = "assigned_to_role", length = 20)
    private String assignedToRole = "ADMIN";

    @Column(length = 2000)
    private String resolutionNotes;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime resolvedAt;

    public DocumentRequest() {}

    public Long getId() { return id; }
    @JsonIgnore public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    @JsonIgnore public User getAssignedToUser() { return assignedToUser; }
    public void setAssignedToUser(User assignedToUser) { this.assignedToUser = assignedToUser; }
    public String getAssignedToRole() { return assignedToRole; }
    public void setAssignedToRole(String assignedToRole) { this.assignedToRole = assignedToRole; }
    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    // Computed
    public String getStudentName() { return student != null ? (student.getFirstName() + " " + student.getLastName()).trim() : ""; }
    public String getStudentUsername() { return student != null ? student.getUsername() : ""; }
    public String getStudentEmail() { return student != null ? student.getEmail() : ""; }
    public String getAssignedToName() {
        if (assignedToUser != null) return (assignedToUser.getFirstName() + " " + assignedToUser.getLastName()).trim();
        return "Administration";
    }
}
