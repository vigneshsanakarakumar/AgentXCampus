package com.agentx.campus.dto;

public class GrievanceRequest {
    private String category;
    private String department;
    private String urgency;
    private String location;
    private String description;

    public GrievanceRequest() {}
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
