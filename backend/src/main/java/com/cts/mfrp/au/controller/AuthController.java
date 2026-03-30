package com.cts.mfrp.au.controller;

import com.cts.mfrp.au.dto.LoginRequest;
import com.cts.mfrp.au.dto.LoginResponse;
import com.cts.mfrp.au.model.User;
import com.cts.mfrp.au.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class AuthController {
    @Autowired
    private UserService userService;
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
        User user = userService.login(request.getEmail(),request.getPassword());
        if(user == null){
            return ResponseEntity.status(401).body("Invalid id credentials");
        }
        LoginResponse response = new LoginResponse("Login Successful",user.getRole());

        return ResponseEntity.ok(response);
    }
}
