package com.agentx.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "hostel_allocations")
public class HostelAllocation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private HostelRoom room;

    @Column(nullable = false) private LocalDate fromDate;
    private LocalDate toDate;
    /** ACTIVE | VACATED */
    @Column(nullable = false, length = 20) private String status = "ACTIVE";

    public HostelAllocation() {}
    public Long getId() { return id; }
    @JsonIgnore public User getStudent() { return student; }
    public void setStudent(User v) { student = v; }
    public HostelRoom getRoom() { return room; }
    public void setRoom(HostelRoom v) { room = v; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate v) { fromDate = v; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate v) { toDate = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }

    // Computed
    public String getRoomNumber()    { return room != null ? room.getRoomNumber() : null; }
    public String getBlockName()     { return room != null ? room.getBlockName() : null; }
    public String getWardenName()    { return room != null ? room.getWardenName() : null; }
    public String getWardenContact() { return room != null ? room.getWardenContact() : null; }
    public String getAmenities()     { return room != null ? room.getAmenities() : null; }
}
