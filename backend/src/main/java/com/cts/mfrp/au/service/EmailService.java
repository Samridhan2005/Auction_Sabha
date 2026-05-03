package com.cts.mfrp.au.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public boolean sendVerifierCredentialsEmail(String toEmail, String name, String tempPassword) {
        if (mailSender == null || fromEmail.isBlank() || isPlaceholderEmail(fromEmail)) {
            System.out.printf("[EMAIL] Verifier approved — To: %s | Temp Password: %s%n", toEmail, tempPassword);
            return false;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(toEmail);
            msg.setSubject("Auction Sabha — Your Verifier Application Has Been Approved!");

            String body = "Dear " + name + ",\n\n" +
                    "Congratulations! Your application to become a Product Verifier on Auction Sabha has been APPROVED.\n\n" +
                    "Here are your login credentials:\n" +
                    "  Email:    " + toEmail + "\n" +
                    "  Password: " + tempPassword + "\n\n" +
                    "Please log in at http://localhost:4200/login and change your password immediately using the Change Password option.\n\n" +
                    "As a Verifier, you can review and approve/reject product submissions from sellers.\n\n" +
                    "Regards,\nAuction Sabha Team";
            msg.setText(body);
            mailSender.send(msg);
            return true;
        } catch (Exception e) {
            System.err.println("[EMAIL] Failed to send verifier credentials to " + toEmail + ": " + e.getMessage());
            return false;
        }
    }

    private boolean isPlaceholderEmail(String email) {
        String lower = email.toLowerCase();
        return lower.contains("your") || lower.contains("example") || lower.contains("placeholder");
    }

    public void sendVerifierRejectionEmail(String toEmail, String name, String remarks) {
        if (mailSender == null || fromEmail.isBlank() || isPlaceholderEmail(fromEmail)) {
            System.out.printf("[EMAIL] Verifier rejected — To: %s | Remarks: %s%n", toEmail, remarks);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(toEmail);
            msg.setSubject("Auction Sabha — Update on Your Verifier Application");

            StringBuilder body = new StringBuilder();
            body.append("Dear ").append(name).append(",\n\n");
            body.append("Thank you for your interest in becoming a Product Verifier on Auction Sabha.\n\n");
            body.append("After reviewing your application, we regret to inform you that it has not been approved at this time.\n\n");
            if (remarks != null && !remarks.isBlank()) {
                body.append("Feedback: ").append(remarks).append("\n\n");
            }
            body.append("You are welcome to apply again in the future with updated qualifications.\n\n");
            body.append("Regards,\nAuction Sabha Team");
            msg.setText(body.toString());
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("[EMAIL] Failed to send verifier rejection to " + toEmail + ": " + e.getMessage());
        }
    }

    public void sendProductStatusEmail(String toEmail, String sellerName,
                                       String productName, String status,
                                       String remarks, String confirmedSlot) {
        if (mailSender == null || fromEmail.isBlank() || isPlaceholderEmail(fromEmail)) {
            System.out.printf("[EMAIL] To: %s | Product: %s | Status: %s%n",
                    toEmail, productName, status);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(toEmail);
            msg.setSubject("Auction Sabha — Your product has been " + status + ": " + productName);

            StringBuilder body = new StringBuilder();
            body.append("Dear ").append(sellerName).append(",\n\n");

            if ("APPROVED".equalsIgnoreCase(status)) {
                body.append("Great news! Your product '").append(productName)
                    .append("' has been APPROVED and scheduled for auction.\n\n");
                if (confirmedSlot != null && !confirmedSlot.isBlank()) {
                    body.append("Scheduled Auction Slot: ").append(confirmedSlot).append("\n\n");
                }
                body.append("Make sure your product is ready. The auction will go live at the scheduled time.\n\n");
            } else {
                body.append("We're sorry, your product '").append(productName)
                    .append("' has been REJECTED.\n\n");
                if (remarks != null && !remarks.isBlank()) {
                    body.append("Reason: ").append(remarks).append("\n\n");
                }
                body.append("You may re-submit after addressing the feedback above.\n\n");
            }

            body.append("Regards,\nAuction Sabha Team");
            msg.setText(body.toString());
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("[EMAIL] Failed to send email to " + toEmail + ": " + e.getMessage());
        }
    }
}
