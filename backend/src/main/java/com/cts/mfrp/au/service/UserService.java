package com.cts.mfrp.au.service;

import com.cts.mfrp.au.dto.RegisterRequest;
import com.cts.mfrp.au.exception.DuplicateEmailException;
import com.cts.mfrp.au.model.User;
import com.cts.mfrp.au.model.Wallet;
import com.cts.mfrp.au.repository.UserRepository;
import com.cts.mfrp.au.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

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

        User savedUser = userRepository.save(newUser);

        Wallet wallet = new Wallet();
        wallet.setUserId(savedUser.getUserId());
        wallet.setAvailableBalance(0);
        wallet.setFrozenBalance(0);
        wallet.setLastUpdated(new Date());
        walletRepository.save(wallet);

        return savedUser;
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
