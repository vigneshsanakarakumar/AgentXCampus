package com.agentx.campus.dto;

import java.time.LocalDate;

public class CourseworkRequestDto {
    private String title;
    private String description;
    private String subjectCode;
    private String subjectName;
    private LocalDate dueDate;
    private String priority = "MEDIUM"; // HIGH, MEDIUM, LOW
    private int maxMarks = 100;

    public CourseworkRequestDto() {}

    public CourseworkRequestDto(String title, String description, String subjectCode, String subjectName, LocalDate dueDate, String priority, int maxMarks) {
        this.title = title;
        this.description = description;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.dueDate = dueDate;
        this.priority = priority;
        this.maxMarks = maxMarks;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public int getMaxMarks() { return maxMarks; }
    public void setMaxMarks(int maxMarks) { this.maxMarks = maxMarks; }
}
