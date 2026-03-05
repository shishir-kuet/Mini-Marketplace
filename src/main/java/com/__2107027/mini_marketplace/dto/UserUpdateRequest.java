package com.__2107027.mini_marketplace.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UserUpdateRequest {

    @Email(message = "Must be a valid email")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password; // optional — only update if provided

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
