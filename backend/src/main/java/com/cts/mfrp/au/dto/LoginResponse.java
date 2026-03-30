package com.cts.mfrp.au.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data

public class LoginResponse {


    private String message;
    private String role;


    public LoginResponse(String loginSuccessful, String role) {
    }
}
