package com.agentx.campus.dto;

public class MentorAssignmentRequest {
    private Long facultyId; // User ID or Faculty Profile ID
    private String assignedDepartment;
    private String assignedSection;

    public MentorAssignmentRequest() {}

    public Long getFacultyId() { return facultyId; }
    public void setFacultyId(Long facultyId) { this.facultyId = facultyId; }

    public String getAssignedDepartment() { return assignedDepartment; }
    public void setAssignedDepartment(String assignedDepartment) { this.assignedDepartment = assignedDepartment; }

    public String getAssignedSection() { return assignedSection; }
    public void setAssignedSection(String assignedSection) { this.assignedSection = assignedSection; }
}
