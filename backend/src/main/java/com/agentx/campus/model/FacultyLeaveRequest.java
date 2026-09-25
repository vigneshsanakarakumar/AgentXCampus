package com.agentx.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "faculty_leave_requests")
public class FacultyLeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    @JsonIgnore
    private User faculty;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(nullable = false, length = 50)
    private String leaveType; // CASUAL_LEAVE, ON_DUTY, MEDICAL_LEAVE, PERMISSION

    @Column(nullable = false)
    private LocalDate fromDate;

    @Column(nullable = false)
    private LocalDate toDate;

    @Column(length = 100)
    private String substituteFacultyName;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hod_user_id")
    @JsonIgnore
    private User hodUser;

    @Column(length = 1000)
    private String resolutionNotes;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime resolvedAt;

    public FacultyLeaveRequest() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getFaculty() { return faculty; }
    public void setFaculty(User faculty) { this.faculty = faculty; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    public String getSubstituteFacultyName() { return substituteFacultyName; }
    public void setSubstituteFacultyName(String substituteFacultyName) { this.substituteFacultyName = substituteFacultyName; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getHodUser() { return hodUser; }
    public void setHodUser(User hodUser) { this.hodUser = hodUser; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    // Computed JSON properties
    public Long getFacultyId() {
        return faculty != null ? faculty.getId() : null;
    }

    public String getFacultyName() {
        return faculty != null ? faculty.getFirstName() + " " + faculty.getLastName() : "Faculty Member";
    }

    public String getFacultyUsername() {
        return faculty != null ? faculty.getUsername() : "";
    }

    public String getFacultyEmail() {
        return faculty != null ? faculty.getEmail() : "";
    }

    public String getHodName() {
        return hodUser != null ? hodUser.getFirstName() + " " + hodUser.getLastName() : "Department HOD";
    }
}
