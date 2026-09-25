package com.agentx.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "attendance_entries",
       uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "student_id"}))
public class AttendanceEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @JsonIgnore
    private AttendanceSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private User student;

    /** PRESENT | ABSENT | LEAVE | OD | LATE | MEDICAL_LEAVE */
    @Column(nullable = false, length = 20)
    private String status = "PRESENT";

    @Column(length = 255)
    private String remarks;

    public AttendanceEntry() {}

    // --- Getters / Setters ---
    public Long getId() { return id; }
    public AttendanceSession getSession() { return session; }
    public void setSession(AttendanceSession session) { this.session = session; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    // Computed (not persisted)
    public Long getStudentId() { return student != null ? student.getId() : null; }
    public String getStudentName() { return student != null ? (student.getFirstName() + " " + student.getLastName()).trim() : null; }
    public String getStudentUsername() { return student != null ? student.getUsername() : null; }
    public String getStudentRollNumber() {
        // StudentProfile lookup would be needed for rollNumber; returning username as fallback
        return student != null ? student.getUsername() : null;
    }
    public Long getSessionId() { return session != null ? session.getId() : null; }
    public String getSubjectCode() { return session != null ? session.getSubjectCode() : null; }
    public String getSubjectName() { return session != null ? session.getSubjectName() : null; }
    public java.time.LocalDate getSessionDate() { return session != null ? session.getSessionDate() : null; }
    public String getPeriod() { return session != null ? session.getPeriod() : null; }
}
