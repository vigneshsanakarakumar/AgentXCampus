package com.agentx.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "faculty_profiles")
public class FacultyProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(unique = true, length = 30)
    private String employeeId;

    @Column(nullable = false, length = 50)
    private String department;

    private String designation;
    private String assignedDepartment;
    private String assignedSection;
    private boolean isMentor = true;
    private String cabinNumber;

    @OneToMany(mappedBy = "facultyProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private java.util.List<FacultyMentorSection> mentorSections = new java.util.ArrayList<>();

    public FacultyProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @JsonIgnore
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getAssignedDepartment() { return assignedDepartment; }
    public void setAssignedDepartment(String assignedDepartment) { this.assignedDepartment = assignedDepartment; }

    public String getAssignedSection() { return assignedSection; }
    public void setAssignedSection(String assignedSection) { this.assignedSection = assignedSection; }

    public boolean isMentor() { return isMentor; }
    public void setMentor(boolean mentor) { isMentor = mentor; }

    public String getCabinNumber() { return cabinNumber; }
    public void setCabinNumber(String cabinNumber) { this.cabinNumber = cabinNumber; }

    public java.util.List<FacultyMentorSection> getMentorSections() { return mentorSections; }
    public void setMentorSections(java.util.List<FacultyMentorSection> mentorSections) { this.mentorSections = mentorSections; }
}
