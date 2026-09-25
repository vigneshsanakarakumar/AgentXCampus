package com.agentx.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_records")
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String courseCode;

    @Column(nullable = false, length = 150)
    private String courseName;

    private int totalClasses = 0;
    private int attendedClasses = 0;
    private double percentage = 100.0;

    private LocalDateTime lastUpdated = LocalDateTime.now();

    public AttendanceRecord() {}

    public AttendanceRecord(User user, String courseCode, String courseName, int totalClasses, int attendedClasses) {
        this.user = user;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.totalClasses = totalClasses;
        this.attendedClasses = attendedClasses;
        this.percentage = totalClasses > 0 ? ((double) attendedClasses / totalClasses) * 100.0 : 100.0;
        this.lastUpdated = LocalDateTime.now();
    }

    public Long getId() { return id; }
    @JsonIgnore
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public int getTotalClasses() { return totalClasses; }
    public void setTotalClasses(int totalClasses) {
        this.totalClasses = totalClasses;
        this.percentage = totalClasses > 0 ? ((double) this.attendedClasses / totalClasses) * 100.0 : 100.0;
    }
    public int getAttendedClasses() { return attendedClasses; }
    public void setAttendedClasses(int attendedClasses) {
        this.attendedClasses = attendedClasses;
        this.percentage = this.totalClasses > 0 ? ((double) attendedClasses / this.totalClasses) * 100.0 : 100.0;
    }
    public double getPercentage() { return Math.round(percentage * 10.0) / 10.0; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
