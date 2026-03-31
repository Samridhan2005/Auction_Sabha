package com.cts.mfrp.au.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // Default constructor kaga
@AllArgsConstructor // Arguments ulla constructor kaga
public class LoginResponse {
    private String message;
    private String role;
    private String token;

//    public LoginResponse(String loginSuccessful, String role) {
//    }
//    public LoginResponse(String loginSuccessful, String role) {
//    }

    // Inga manual constructor potrundha adhai delete pannidunga.
    // Lombok-ey adhai pathukkum.
}