package com.agentx.campus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "timetables")
public class TimetableEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String department;

    @Column(nullable = false, length = 10)
    private String section;

    private int semester = 1;

    @Column(nullable = false, length = 30)
    private String subjectCode;

    @Column(nullable = false, length = 100)
    private String subjectName;

    private String facultyName;
    private Long facultyUserId;

    @Column(nullable = false, length = 20)
    private String dayOfWeek;

    @Column(nullable = false, length = 20)
    private String startTime;

    @Column(nullable = false, length = 20)
    private String endTime;

    private String classroom;

    public TimetableEntry() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Long getFacultyUserId() { return facultyUserId; }
    public void setFacultyUserId(Long facultyUserId) { this.facultyUserId = facultyUserId; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getClassroom() { return classroom; }
    public void setClassroom(String classroom) { this.classroom = classroom; }
}
