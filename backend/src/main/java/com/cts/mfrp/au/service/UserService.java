package com.cts.mfrp.au.service;

import com.cts.mfrp.au.dto.RegisterRequest;
import com.cts.mfrp.au.exception.DuplicateEmailException;
import com.cts.mfrp.au.model.User;
import com.cts.mfrp.au.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public User registerUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new DuplicateEmailException("Email already registered!");
        }

        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());

        // PASSWORD HASHING HERE
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        newUser.setRole(request.getRole().toUpperCase());
        newUser.setPhone(request.getPhone());

        return userRepository.save(newUser);
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    public User findById(int id){
        Optional<User> our= userRepository.findById(id);
        return our.orElse(null);
    }

}
