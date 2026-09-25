package com.agentx.campus.dto;

public class StaffRequestDto {
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String designation;

    public StaffRequestDto() {}

    public StaffRequestDto(String firstName, String lastName, String email, String department, String designation) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.designation = designation;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
}
