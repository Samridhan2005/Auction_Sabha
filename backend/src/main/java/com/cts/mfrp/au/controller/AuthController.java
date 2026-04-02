package com.cts.mfrp.au.controller;

import com.cts.mfrp.au.dto.LoginRequest;
import com.cts.mfrp.au.dto.LoginResponse;
import com.cts.mfrp.au.dto.RegisterRequest;
import com.cts.mfrp.au.model.User;
import com.cts.mfrp.au.security.JwtUtil;
import com.cts.mfrp.au.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // ADD THIS
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
        User user = userService.login(request.getEmail(), request.getPassword());
        if(user == null){
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        // Generate JWT
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        // Return response with token
        LoginResponse response = new LoginResponse("Login Successful", user.getRole(), token);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User registeredUser = userService.registerUser(request);
            return ResponseEntity.ok("User registered successfully with ID: " + registeredUser.getUserId());
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
