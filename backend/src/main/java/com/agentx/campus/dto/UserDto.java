package com.agentx.campus.dto;

import com.agentx.campus.model.Role;

public class UserDto {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private boolean isActive;

    public UserDto(Long id, String username, String email, Role role, String firstName, String lastName, String avatarUrl, boolean isActive) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatarUrl = avatarUrl;
        this.isActive = isActive;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getAvatarUrl() { return avatarUrl; }
    public boolean isActive() { return isActive; }
}
