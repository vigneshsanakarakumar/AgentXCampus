package com.agentx.campus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bus_routes")
public class BusRoute {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 20)  private String routeNumber;
    @Column(nullable = false, length = 150) private String routeName;
    @Column(nullable = false, length = 100) private String startPoint;
    @Column(nullable = false, length = 100) private String endPoint;
    @Column(length = 20) private String departureTime;
    @Column(length = 20) private String returnTime;
    @Column(length = 100) private String driverName;
    @Column(length = 20) private String driverContact;

    public BusRoute() {}
    public Long getId() { return id; }
    public String getRouteNumber() { return routeNumber; }
    public void setRouteNumber(String v) { routeNumber = v; }
    public String getRouteName() { return routeName; }
    public void setRouteName(String v) { routeName = v; }
    public String getStartPoint() { return startPoint; }
    public void setStartPoint(String v) { startPoint = v; }
    public String getEndPoint() { return endPoint; }
    public void setEndPoint(String v) { endPoint = v; }
    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String v) { departureTime = v; }
    public String getReturnTime() { return returnTime; }
    public void setReturnTime(String v) { returnTime = v; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String v) { driverName = v; }
    public String getDriverContact() { return driverContact; }
    public void setDriverContact(String v) { driverContact = v; }
}
