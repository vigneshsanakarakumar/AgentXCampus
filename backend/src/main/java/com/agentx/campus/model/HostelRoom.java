package com.agentx.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "hostel_rooms")
public class HostelRoom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id", nullable = false)
    @JsonIgnore
    private HostelBlock block;

    @Column(nullable = false, length = 20) private String roomNumber;
    private int capacity = 2;
    private int occupied = 0;
    @Column(length = 500) private String amenities;

    public HostelRoom() {}
    public Long getId() { return id; }
    @JsonIgnore public HostelBlock getBlock() { return block; }
    public void setBlock(HostelBlock v) { block = v; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String v) { roomNumber = v; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int v) { capacity = v; }
    public int getOccupied() { return occupied; }
    public void setOccupied(int v) { occupied = v; }
    public String getAmenities() { return amenities; }
    public void setAmenities(String v) { amenities = v; }

    // Computed
    public Long getBlockId() { return block != null ? block.getId() : null; }
    public String getBlockName() { return block != null ? block.getBlockName() : null; }
    public String getWardenName() { return block != null ? block.getWardenName() : null; }
    public String getWardenContact() { return block != null ? block.getWardenContact() : null; }
    public int getAvailable() { return capacity - occupied; }
}
