package com.cts.mfrp.au.dto;
import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;

    public String getRole() {
        return role;
    }

    private String role;

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
}
