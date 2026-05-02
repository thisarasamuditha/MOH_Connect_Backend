package com.moh.moh_backend.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email service for sending notifications
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:no-reply@moh.gov.lk}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send mother credentials to email
     */
    public void sendMotherCredentials(String toEmail, String username, String password, String motherName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("MOH Connect - Your Account Credentials");
            
            String body = "Dear " + motherName + ",\n\n" +
                    "Your account has been created in the MOH Connect system.\n\n" +
                    "Login Credentials:\n" +
                    "Username: " + username + "\n" +
                    "Password: " + password + "\n\n" +
                    "Please change your password after first login.\n\n" +
                    "Best regards,\n" +
                    "MOH Connect Team";
            
            message.setText(body);
            mailSender.send(message);
            System.out.println("✓ Credentials sent to " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
            // Don't throw exception - registration should still succeed even if email fails
        }
    }
}
