package com.cts.mfrp.au.service;

import com.cts.mfrp.au.dto.RegisterRequest;
import com.cts.mfrp.au.model.User;
import com.cts.mfrp.au.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User login(String email,String password){
        User user = userRepository.findByEmail(email);
        if(user!=null && user.getPassword().equals(password)){
            return user;
        }
        return null;
    }

    public User registerUser(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new RuntimeException("Email already registered!");
        }

        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(request.getPassword()); // Later: add password encoding
        newUser.setRole(request.getRole().toUpperCase());
        newUser.setPhone(request.getPhone());

        return userRepository.save(newUser);
    }


}
