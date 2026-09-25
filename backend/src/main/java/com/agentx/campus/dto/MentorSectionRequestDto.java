package com.agentx.campus.dto;

public class MentorSectionRequestDto {
    private String department;
    private String section;
    private Integer semester;
    private String academicYear = "2025-2026";

    public MentorSectionRequestDto() {}

    public MentorSectionRequestDto(String department, String section, Integer semester, String academicYear) {
        this.department = department;
        this.section = section;
        this.semester = semester;
        this.academicYear = academicYear != null ? academicYear : "2025-2026";
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
}
