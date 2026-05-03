package com.cts.mfrp.au.service;

import com.cts.mfrp.au.dto.VerifierApplicationRequest;
import com.cts.mfrp.au.dto.VerifierApprovalResponse;
import com.cts.mfrp.au.model.User;
import com.cts.mfrp.au.model.VerifierApplication;
import com.cts.mfrp.au.model.Wallet;
import com.cts.mfrp.au.repository.UserRepository;
import com.cts.mfrp.au.repository.VerifierApplicationRepository;
import com.cts.mfrp.au.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class VerifierApplicationService {

    @Autowired
    private VerifierApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$!";

    public VerifierApplication submitApplication(VerifierApplicationRequest request) {
        if (applicationRepository.findByEmail(request.getEmail()) != null) {
            throw new RuntimeException("An application with this email already exists.");
        }
        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new RuntimeException("This email is already registered as a user.");
        }

        VerifierApplication app = new VerifierApplication();
        app.setName(request.getName());
        app.setEmail(request.getEmail());
        app.setPhone(request.getPhone());
        app.setAge(request.getAge());
        app.setQualification(request.getQualification());
        app.setExperienceYears(request.getExperienceYears());
        app.setDomainsInterested(request.getDomainsInterested());
        app.setMotivation(request.getMotivation());
        app.setPortfolioUrl(request.getPortfolioUrl());
        app.setStatus("PENDING");
        app.setSubmittedAt(LocalDateTime.now());

        return applicationRepository.save(app);
    }

    public List<VerifierApplication> getAllApplications() {
        return applicationRepository.findAllByOrderBySubmittedAtDesc();
    }

    public List<VerifierApplication> getPendingApplications() {
        return applicationRepository.findByStatusOrderBySubmittedAtDesc("PENDING");
    }

    public VerifierApprovalResponse approveApplication(Long id, String remarks) {
        VerifierApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!"PENDING".equals(app.getStatus())) {
            throw new RuntimeException("Application has already been processed.");
        }

        if (userRepository.findByEmail(app.getEmail()) != null) {
            throw new RuntimeException("A user account already exists for this email. Application may have been approved already.");
        }

        String tempPassword = generatePassword(10);

        User verifier = new User();
        verifier.setName(app.getName());
        verifier.setEmail(app.getEmail());
        verifier.setPhone(app.getPhone());
        verifier.setRole("VERIFIER");
        verifier.setPassword(passwordEncoder.encode(tempPassword));
        User savedUser = userRepository.save(verifier);

        Wallet wallet = new Wallet();
        wallet.setUserId(savedUser.getUserId());
        wallet.setAvailableBalance(0);
        wallet.setFrozenBalance(0);
        wallet.setLastUpdated(new Date());
        walletRepository.save(wallet);

        app.setStatus("APPROVED");
        app.setAdminRemarks(remarks);
        VerifierApplication saved = applicationRepository.save(app);

        boolean emailSent = emailService.sendVerifierCredentialsEmail(app.getEmail(), app.getName(), tempPassword);

        return new VerifierApprovalResponse(saved, tempPassword, emailSent);
    }

    public VerifierApplication rejectApplication(Long id, String remarks) {
        VerifierApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!"PENDING".equals(app.getStatus())) {
            throw new RuntimeException("Application has already been processed.");
        }

        app.setStatus("REJECTED");
        app.setAdminRemarks(remarks);
        VerifierApplication saved = applicationRepository.save(app);

        emailService.sendVerifierRejectionEmail(app.getEmail(), app.getName(), remarks);

        return saved;
    }

    private String generatePassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }
        return sb.toString();
    }
}
