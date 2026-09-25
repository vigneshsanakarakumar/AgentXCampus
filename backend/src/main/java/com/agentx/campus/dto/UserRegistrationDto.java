package com.agentx.campus.dto;

import com.agentx.campus.model.Role;

public class UserRegistrationDto {
    private Role role = Role.STUDENT; // STUDENT or FACULTY
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
    private String confirmPassword;
    private String department;

    // Student fields
    private String rollNumber;
    private String section = "A";
    private Integer year = 1;
    private Integer semester = 1;

    // Faculty fields
    private String designation;
    private String specialization;
    private String cabinNumber;

    public UserRegistrationDto() {}

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getCabinNumber() { return cabinNumber; }
    public void setCabinNumber(String cabinNumber) { this.cabinNumber = cabinNumber; }
}
