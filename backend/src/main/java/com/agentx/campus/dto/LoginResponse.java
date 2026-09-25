package com.agentx.campus.dto;

import com.agentx.campus.model.Role;

public class LoginResponse {
    private String token;
    private String refreshToken;
    private Long id;
    private String username;
    private String email;
    private Role role;
    private String firstName;
    private String lastName;

    public LoginResponse(String token, String refreshToken, Long id, String username, String email, Role role, String firstName, String lastName) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getToken() { return token; }
    public String getRefreshToken() { return refreshToken; }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
}
