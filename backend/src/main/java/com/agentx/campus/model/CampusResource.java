package com.agentx.campus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "campus_resources")
public class CampusResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String building;

    @Column(nullable = false, length = 30)
    private String roomNumber;

    @Column(nullable = false, length = 30)
    private String type; // CLASSROOM, LAB, SEMINAR_HALL, AUDITORIUM

    private int capacity = 60;

    @Column(nullable = false, length = 30)
    private String status = "AVAILABLE"; // AVAILABLE, OCCUPIED, MAINTENANCE

    public CampusResource() {}

    public CampusResource(String name, String building, String roomNumber, String type, int capacity, String status) {
        this.name = name;
        this.building = building;
        this.roomNumber = roomNumber;
        this.type = type;
        this.capacity = capacity;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
