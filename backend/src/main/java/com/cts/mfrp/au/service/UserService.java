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
import java.util.Map;
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

    public void updateProfile(int userId, Map<String, Object> body) {
        User user = findById(userId);
        if (user == null) throw new RuntimeException("User not found");
        if (body.containsKey("name") && body.get("name") != null)
            user.setName(body.get("name").toString());
        if (body.containsKey("phone") && body.get("phone") != null)
            user.setPhone(body.get("phone").toString());
        if (body.containsKey("age") && body.get("age") != null) {
            try { user.setAge(Integer.parseInt(body.get("age").toString())); } catch (NumberFormatException ignored) {}
        }
        if (body.containsKey("place") && body.get("place") != null)
            user.setPlace(body.get("place").toString());
        if (body.containsKey("about") && body.get("about") != null)
            user.setAbout(body.get("about").toString());
        userRepository.save(user);
    }

    public void changePassword(int userId, String oldPassword, String newPassword) {
        User user = findById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

}
