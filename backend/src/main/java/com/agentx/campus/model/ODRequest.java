package com.agentx.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "od_requests")
public class ODRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private User student;

    @Column(nullable = false, length = 200)
    private String eventName;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Column(length = 100)
    private String periods;

    @Column(length = 200)
    private String location;

    @Column(nullable = false, length = 2000)
    private String reason;

    @Column(length = 500)
    private String proofUrl;

    /** PENDING | APPROVED | REJECTED */
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

    public ODRequest() {}

    // --- Getters / Setters ---
    public Long getId() { return id; }
    @JsonIgnore public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public String getPeriods() { return periods; }
    public void setPeriods(String periods) { this.periods = periods; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getProofUrl() { return proofUrl; }
    public void setProofUrl(String proofUrl) { this.proofUrl = proofUrl; }
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
    public String getAssignedToName() {
        if (assignedToUser != null) return (assignedToUser.getFirstName() + " " + assignedToUser.getLastName()).trim();
        return "ADMIN".equals(assignedToRole) ? "Administration" : assignedToRole;
    }
}
