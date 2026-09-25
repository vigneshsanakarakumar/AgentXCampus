package com.agentx.campus.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String courseCode;

    @Column(nullable = false, length = 150)
    private String courseName;

    @Column(nullable = false, length = 80)
    private String department;

    private int semester = 5;
    private int credits = 4;

    private String facultyName;
    @Column(length = 1000)
    private String description;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Course() {}

    public Course(String courseCode, String courseName, String department, int semester, int credits, String facultyName, String description) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.department = department;
        this.semester = semester;
        this.credits = credits;
        this.facultyName = facultyName;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
