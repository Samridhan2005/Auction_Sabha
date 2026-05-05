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
import java.util.Map;

@RestController
@RequestMapping("/api/auth") // ADD THIS
@CrossOrigin(origins = "*")
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

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        LoginResponse response = new LoginResponse("Login Successful", user.getRole(), token, user.getUserId(), user.getName());

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

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable int userId) {
        User user = userService.findById(userId);
        if (user == null) return ResponseEntity.status(404).body("User not found");
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateProfile(@PathVariable int userId, @RequestBody Map<String, Object> body) {
        try {
            userService.updateProfile(userId, body);
            User updated = userService.findById(userId);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, Object> body) {
        try {
            int userId = Integer.parseInt(body.get("userId").toString());
            String oldPassword = body.get("oldPassword").toString();
            String newPassword = body.get("newPassword").toString();
            userService.changePassword(userId, oldPassword, newPassword);
            return ResponseEntity.ok("Password updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
