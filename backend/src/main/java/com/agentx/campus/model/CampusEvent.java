package com.agentx.campus.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "campus_events")
public class CampusEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String category;
    @Column(length = 1000)
    private String description;
    private String location;
    private LocalDate eventDate;
    private String eventTime;
    private String organizer;
    private int registeredCount;

    public CampusEvent() {}

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public String getEventTime() { return eventTime; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }
    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }
    public int getRegisteredCount() { return registeredCount; }
    public void setRegisteredCount(int registeredCount) { this.registeredCount = registeredCount; }
}
