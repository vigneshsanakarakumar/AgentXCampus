package com.agentx.campus.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 1500)
    private String description;

    @Column(nullable = false, length = 20)
    private String subjectCode;

    @Column(nullable = false, length = 150)
    private String subjectName;

    @Column(nullable = false, length = 80)
    private String department;

    private int semester = 5;
    private String section; // e.g. "C" or "ALL"
    private String facultyName;

    private LocalDate assignedDate;
    private LocalDate dueDate;

    @Column(length = 20)
    private String priority = "MEDIUM"; // HIGH, MEDIUM, LOW

    @Column(length = 20)
    private String status = "ACTIVE"; // ACTIVE, SUBMITTED, GRADED, CLOSED

    private int maxMarks = 100;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Assignment() {}

    public Assignment(String title, String description, String subjectCode, String subjectName,
                      String department, int semester, String section, String facultyName,
                      LocalDate assignedDate, LocalDate dueDate, String priority, int maxMarks) {
        this.title = title;
        this.description = description;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.department = department;
        this.semester = semester;
        this.section = section;
        this.facultyName = facultyName;
        this.assignedDate = assignedDate;
        this.dueDate = dueDate;
        this.priority = priority;
        this.maxMarks = maxMarks;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }
    public LocalDate getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDate assignedDate) { this.assignedDate = assignedDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getMaxMarks() { return maxMarks; }
    public void setMaxMarks(int maxMarks) { this.maxMarks = maxMarks; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
