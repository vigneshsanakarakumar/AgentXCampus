package com.agentx.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gate_pass_requests")
public class GatePassRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private User student;

    @Column(nullable = false, length = 2000) private String purpose;
    @Column(nullable = false) private LocalDateTime outDateTime;
    @Column(nullable = false) private LocalDateTime expectedReturnDateTime;
    @Column(length = 200) private String destination;

    /** PENDING | APPROVED | REJECTED */
    @Column(nullable = false, length = 20) private String status = "PENDING";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    @JsonIgnore
    private User approvedBy;

    @Column(name = "assigned_to_role", length = 20) private String assignedToRole = "ADMIN";
    private LocalDateTime createdAt = LocalDateTime.now();

    public GatePassRequest() {}
    public Long getId() { return id; }
    @JsonIgnore public User getStudent() { return student; }
    public void setStudent(User v) { student = v; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String v) { purpose = v; }
    public LocalDateTime getOutDateTime() { return outDateTime; }
    public void setOutDateTime(LocalDateTime v) { outDateTime = v; }
    public LocalDateTime getExpectedReturnDateTime() { return expectedReturnDateTime; }
    public void setExpectedReturnDateTime(LocalDateTime v) { expectedReturnDateTime = v; }
    public String getDestination() { return destination; }
    public void setDestination(String v) { destination = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    @JsonIgnore public User getApprovedBy() { return approvedBy; }
    public void setApprovedBy(User v) { approvedBy = v; }
    public String getAssignedToRole() { return assignedToRole; }
    public void setAssignedToRole(String v) { assignedToRole = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Computed
    public String getStudentName()     { return student != null ? (student.getFirstName() + " " + student.getLastName()).trim() : ""; }
    public String getStudentUsername() { return student != null ? student.getUsername() : ""; }
    public String getApprovedByName()  { return approvedBy != null ? (approvedBy.getFirstName() + " " + approvedBy.getLastName()).trim() : null; }
}
