package com.agentx.campus.model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "grievances")
public class Grievance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ticketNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String department;
    private String category;
    private String urgency;
    private String status;
    private String location;

    @Column(length = 1000)
    private String description;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    @JsonIgnore
    private User assignedToUser;

    @Column(name = "assigned_to_role")
    private String assignedToRole = "ADMIN";

    @Column(name = "assigned_to")
    private String assignedTo = "Administration";

    @Column(name = "routed_to")
    private String routedTo = "ADMIN";

    @Column(name = "resolution_notes", length = 1000)
    private String resolutionNotes;

    public Grievance() {}

    public Long getId() { return id; }
    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
    @JsonIgnore
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public User getAssignedToUser() { return assignedToUser; }
    public void setAssignedToUser(User assignedToUser) { this.assignedToUser = assignedToUser; }
    public String getAssignedToRole() { return assignedToRole; }
    public void setAssignedToRole(String assignedToRole) { this.assignedToRole = assignedToRole; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public String getRoutedTo() { return routedTo; }
    public void setRoutedTo(String routedTo) { this.routedTo = routedTo; }
    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public String getSubmitterName() {
        return user != null ? (user.getFirstName() + " " + user.getLastName()).trim() : "Unknown";
    }

    public String getSubmitterUsername() {
        return user != null ? user.getUsername() : "";
    }

    public String getSubmitterEmail() {
        return user != null ? user.getEmail() : "";
    }

    public String getSubmitterRole() {
        return user != null && user.getRole() != null ? user.getRole().name() : "";
    }

    public String getAssignedToName() {
        if (assignedToUser != null) {
            return (assignedToUser.getFirstName() + " " + assignedToUser.getLastName()).trim();
        }
        return assignedTo != null ? assignedTo : "Administration";
    }

    public String getAssignedToUsername() {
        return assignedToUser != null ? assignedToUser.getUsername() : null;
    }
}
