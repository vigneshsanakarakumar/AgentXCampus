package com.agentx.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_registration_requests")
public class UserRegistrationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role; // STUDENT or FACULTY

    @Column(nullable = false, length = 100)
    private String department;

    // Student fields
    @Column(length = 50)
    private String rollNumber;

    @Column(length = 10)
    private String section;

    private Integer year;

    private Integer semester;

    // Faculty fields
    @Column(length = 100)
    private String designation;

    @Column(length = 255)
    private String specialization;

    @Column(length = 50)
    private String cabinNumber;

    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hod_user_id")
    @JsonIgnore
    private User hodUser;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime resolvedAt;

    public UserRegistrationRequest() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getCabinNumber() { return cabinNumber; }
    public void setCabinNumber(String cabinNumber) { this.cabinNumber = cabinNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getHodUser() { return hodUser; }
    public void setHodUser(User hodUser) { this.hodUser = hodUser; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    // Computed getters for frontend serialization
    public String getApplicantName() {
        return firstName + " " + lastName;
    }

    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    public String getHodName() {
        return hodUser != null ? hodUser.getFirstName() + " " + hodUser.getLastName() : null;
    }
}
