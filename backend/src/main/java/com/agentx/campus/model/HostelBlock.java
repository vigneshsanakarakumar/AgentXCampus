package com.agentx.campus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "hostel_blocks")
public class HostelBlock {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50) private String blockName;
    private int totalRooms;
    private int occupiedRooms;
    @Column(length = 100) private String wardenName;
    @Column(length = 20)  private String wardenContact;
    /** MALE | FEMALE | MIXED */
    @Column(length = 10)  private String gender = "MIXED";

    public HostelBlock() {}
    public Long getId() { return id; }
    public String getBlockName() { return blockName; }
    public void setBlockName(String v) { blockName = v; }
    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int v) { totalRooms = v; }
    public int getOccupiedRooms() { return occupiedRooms; }
    public void setOccupiedRooms(int v) { occupiedRooms = v; }
    public String getWardenName() { return wardenName; }
    public void setWardenName(String v) { wardenName = v; }
    public String getWardenContact() { return wardenContact; }
    public void setWardenContact(String v) { wardenContact = v; }
    public String getGender() { return gender; }
    public void setGender(String v) { gender = v; }
}
