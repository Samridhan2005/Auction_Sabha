package com.cts.mfrp.au.dto;
import lombok.Data;

@Data
public class LoginResponse {
    private String message;
    private String role;
    private String token;
    private int userid;
    public LoginResponse(String message, String role, String token, int userid) {
        this.message = message;
        this.role = role;
        this.token = token;
        this.userid=userid;
    }
}