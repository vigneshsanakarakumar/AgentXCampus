package com.agentx.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_schedules")
public class ExamSchedule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)  private String subjectCode;
    @Column(nullable = false, length = 150) private String subjectName;
    @Column(nullable = false, length = 100) private String department;
    @Column(nullable = false, length = 10)  private String section;
    private int semester = 1;
    @Column(nullable = false) private LocalDate examDate;
    @Column(nullable = false, length = 20) private String startTime;
    @Column(nullable = false, length = 20) private String endTime;
    @Column(nullable = false, length = 50) private String room;
    /** INTERNAL | EXTERNAL | PLACEMENT_TEST */
    @Column(nullable = false, length = 30) private String examType = "INTERNAL";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnore
    private User createdBy;

    private LocalDateTime createdAt = LocalDateTime.now();

    public ExamSchedule() {}
    public Long getId() { return id; }
    public void setId(Long v) { id = v; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String v) { subjectCode = v; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String v) { subjectName = v; }
    public String getDepartment() { return department; }
    public void setDepartment(String v) { department = v; }
    public String getSection() { return section; }
    public void setSection(String v) { section = v; }
    public int getSemester() { return semester; }
    public void setSemester(int v) { semester = v; }
    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate v) { examDate = v; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String v) { startTime = v; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String v) { endTime = v; }
    public String getRoom() { return room; }
    public void setRoom(String v) { room = v; }
    public String getExamType() { return examType; }
    public void setExamType(String v) { examType = v; }
    @JsonIgnore public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User v) { createdBy = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Computed
    public String getCreatedByName() { return createdBy != null ? (createdBy.getFirstName() + " " + createdBy.getLastName()).trim() : "Admin"; }
}
