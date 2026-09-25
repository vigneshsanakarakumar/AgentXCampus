package com.agentx.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "student_bus_passes")
public class StudentBusPass {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private BusRoute route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boarding_stop_id")
    private BusStop boardingStop;

    @Column(nullable = false) private LocalDate validFrom;
    @Column(nullable = false) private LocalDate validTo;
    /** ACTIVE | EXPIRED | CANCELLED */
    @Column(nullable = false, length = 20) private String status = "ACTIVE";

    public StudentBusPass() {}
    public Long getId() { return id; }
    @JsonIgnore public User getStudent() { return student; }
    public void setStudent(User v) { student = v; }
    public BusRoute getRoute() { return route; }
    public void setRoute(BusRoute v) { route = v; }
    public BusStop getBoardingStop() { return boardingStop; }
    public void setBoardingStop(BusStop v) { boardingStop = v; }
    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate v) { validFrom = v; }
    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate v) { validTo = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }

    // Computed
    public String getRouteName()        { return route != null ? route.getRouteName() : null; }
    public String getRouteNumber()      { return route != null ? route.getRouteNumber() : null; }
    public String getDepartureTime()    { return route != null ? route.getDepartureTime() : null; }
    public String getReturnTime()       { return route != null ? route.getReturnTime() : null; }
    public String getBoardingStopName() { return boardingStop != null ? boardingStop.getStopName() : null; }
    public String getStudentName()      { return student != null ? (student.getFirstName() + " " + student.getLastName()).trim() : ""; }
}
