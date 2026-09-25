package com.agentx.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "bus_stops")
public class BusStop {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    @JsonIgnore
    private BusRoute route;

    @Column(nullable = false, length = 100) private String stopName;
    @Column(nullable = false) private int stopOrder;
    @Column(length = 20) private String arrivalTime;

    public BusStop() {}
    public Long getId() { return id; }
    @JsonIgnore public BusRoute getRoute() { return route; }
    public void setRoute(BusRoute v) { route = v; }
    public String getStopName() { return stopName; }
    public void setStopName(String v) { stopName = v; }
    public int getStopOrder() { return stopOrder; }
    public void setStopOrder(int v) { stopOrder = v; }
    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String v) { arrivalTime = v; }

    // Computed
    public Long getRouteId() { return route != null ? route.getId() : null; }
    public String getRouteName() { return route != null ? route.getRouteName() : null; }
}
