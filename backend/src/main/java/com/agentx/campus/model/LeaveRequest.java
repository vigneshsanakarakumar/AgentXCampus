package com.agentx.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private User student;

    /** MEDICAL | PERSONAL | FAMILY | OTHER */
    @Column(nullable = false, length = 30)
    private String leaveType;

    @Column(nullable = false)
    private LocalDate fromDate;

    @Column(nullable = false)
    private LocalDate toDate;

    @Column(length = 100)
    private String period;

    @Column(nullable = false, length = 2000)
    private String reason;

    @Column(length = 500)
    private String attachmentUrl;

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

    public LeaveRequest() {}

    // --- Getters / Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    @JsonIgnore public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
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

    // Computed (not persisted)
    public String getStudentName() { return student != null ? (student.getFirstName() + " " + student.getLastName()).trim() : ""; }
    public String getStudentUsername() { return student != null ? student.getUsername() : ""; }
    public String getStudentEmail() { return student != null ? student.getEmail() : ""; }
    public String getAssignedToName() {
        if (assignedToUser != null) return (assignedToUser.getFirstName() + " " + assignedToUser.getLastName()).trim();
        return "ADMIN".equals(assignedToRole) ? "Administration" : assignedToRole;
    }
    public long getTotalDays() {
        if (fromDate == null || toDate == null) return 1;
        return java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate) + 1;
    }

    public java.util.List<String> getLeaveDates() {
        if (fromDate == null || toDate == null) return java.util.Collections.emptyList();
        java.util.List<String> dates = new java.util.ArrayList<>();
        LocalDate cur = fromDate;
        while (!cur.isAfter(toDate)) {
            dates.add(cur.toString());
            cur = cur.plusDays(1);
        }
        return dates;
    }
}

