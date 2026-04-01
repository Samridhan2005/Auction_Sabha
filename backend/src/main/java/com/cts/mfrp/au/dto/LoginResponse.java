package com.cts.mfrp.au.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class LoginResponse {
    private String message;



    private String role;
    private String token;
    public LoginResponse(String message, String role, String token) {
        this.message = message;
        this.role = role;
        this.token = token;
    }
}