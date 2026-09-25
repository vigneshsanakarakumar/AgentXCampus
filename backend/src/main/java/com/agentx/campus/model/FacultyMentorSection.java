package com.agentx.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "faculty_mentor_sections")
public class FacultyMentorSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_profile_id", nullable = false)
    @JsonIgnoreProperties({"user", "hibernateLazyInitializer", "handler"})
    private FacultyProfile facultyProfile;

    @Column(nullable = false, length = 50)
    private String department;

    @Column(nullable = false, length = 10)
    private String section;

    @Column(nullable = false)
    private Integer semester;

    @Column(length = 20)
    private String academicYear = "2025-2026";

    private LocalDateTime createdAt = LocalDateTime.now();

    public FacultyMentorSection() {}

    public FacultyMentorSection(FacultyProfile facultyProfile, String department, String section, Integer semester, String academicYear) {
        this.facultyProfile = facultyProfile;
        this.department = department;
        this.section = section;
        this.semester = semester;
        this.academicYear = academicYear != null ? academicYear : "2025-2026";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public FacultyProfile getFacultyProfile() { return facultyProfile; }
    public void setFacultyProfile(FacultyProfile facultyProfile) { this.facultyProfile = facultyProfile; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getSectionName() { return section; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
