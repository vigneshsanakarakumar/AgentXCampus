package com.agentx.campus.dto;

public class TimetableRequest {
    private String department;
    private String section;
    private int semester = 1;
    private String subjectCode;
    private String subjectName;
    private String facultyName;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String classroom;

    public TimetableRequest() {}

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getClassroom() { return classroom; }
    public void setClassroom(String classroom) { this.classroom = classroom; }
}
