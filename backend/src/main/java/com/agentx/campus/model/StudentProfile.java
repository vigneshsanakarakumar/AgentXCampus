package com.agentx.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "student_profiles")
public class StudentProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true, length = 30)
    private String rollNumber;

    @Column(nullable = false, length = 50)
    private String department;

    @Column(nullable = false, length = 10)
    private String section;

    private int year = 1;
    private int semester = 1;
    private double cgpa = 0.0;
    private int attendanceRate = 100;
    private String hostelBlock;
    private String roomNumber;

    /** HOSTELLER | DAY_SCHOLAR */
    @Column(name = "student_type", length = 20)
    private String studentType = "DAY_SCHOLAR";

    public StudentProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @JsonIgnore
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public double getCgpa() { return cgpa; }
    public void setCgpa(double cgpa) { this.cgpa = cgpa; }

    public int getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(int attendanceRate) { this.attendanceRate = attendanceRate; }

    public String getHostelBlock() { return hostelBlock; }
    public void setHostelBlock(String hostelBlock) { this.hostelBlock = hostelBlock; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getStudentType() { return studentType; }
    public void setStudentType(String studentType) { this.studentType = studentType; }
}
