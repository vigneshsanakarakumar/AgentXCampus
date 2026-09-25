package com.agentx.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "attendance_sessions")
public class AttendanceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    @JsonIgnore
    private FacultyMentorSection section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    @JsonIgnore
    private User faculty;

    @Column(nullable = false, length = 30)
    private String subjectCode;

    @Column(nullable = false, length = 150)
    private String subjectName;

    @Column(nullable = false)
    private LocalDate sessionDate;

    @Column(nullable = false, length = 50)
    private String period;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<AttendanceEntry> entries;

    public AttendanceSession() {}

    // --- Getters / Setters ---
    public Long getId() { return id; }
    public FacultyMentorSection getSection() { return section; }
    public void setSection(FacultyMentorSection section) { this.section = section; }
    public User getFaculty() { return faculty; }
    public void setFaculty(User faculty) { this.faculty = faculty; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<AttendanceEntry> getEntries() { return entries; }

    // Computed (not persisted)
    public Long getSectionId() { return section != null ? section.getId() : null; }
    public String getDepartment() { return section != null ? section.getDepartment() : null; }
    public String getSectionName() { return section != null ? section.getSection() : null; }
    public String getFacultyName() { return faculty != null ? (faculty.getFirstName() + " " + faculty.getLastName()).trim() : null; }
}
