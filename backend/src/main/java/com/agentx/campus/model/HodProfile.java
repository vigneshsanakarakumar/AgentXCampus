package com.agentx.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hod_profiles")
public class HodProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;

    @Column(nullable = false, unique = true, length = 100)
    private String department;

    @Column(length = 50)
    private String cabinNumber;

    @Column(length = 20)
    private String contactNumber;

    private LocalDateTime createdAt = LocalDateTime.now();

    public HodProfile() {}

    public HodProfile(User user, String department, String cabinNumber, String contactNumber) {
        this.user = user;
        this.department = department;
        this.cabinNumber = cabinNumber;
        this.contactNumber = contactNumber;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getCabinNumber() { return cabinNumber; }
    public void setCabinNumber(String cabinNumber) { this.cabinNumber = cabinNumber; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Computed attributes for JSON serialization
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    public String getHodName() {
        return user != null ? user.getFirstName() + " " + user.getLastName() : "HOD";
    }

    public String getHodEmail() {
        return user != null ? user.getEmail() : "";
    }

    public String getHodUsername() {
        return user != null ? user.getUsername() : "";
    }
}
