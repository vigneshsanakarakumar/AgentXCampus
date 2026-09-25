package com.agentx.campus.dto;

public class LoginRequest {
    private String identifier; // username or email
    private String password;
    private boolean rememberMe;

    public LoginRequest() {}

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public void setUsername(String username) { if (this.identifier == null || this.identifier.isBlank()) this.identifier = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isRememberMe() { return rememberMe; }
    public void setRememberMe(boolean rememberMe) { this.rememberMe = rememberMe; }
}
