package com.moh.moh_backend.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

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
     * @return true if the email was handed to the mail server, false if sending failed
     */
    public boolean sendMotherCredentials(String toEmail, String username, String password, String motherName) {
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
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
            // Don't throw exception - registration should still succeed even if email fails
            return false;
        }
    }

    /**
     * Send a "forgot password" link.
     * @return true if the email was handed to the mail server, false if sending failed
     */
    public boolean sendPasswordResetLink(String toEmail, String username, String resetLink, int validMinutes) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("MOH Connect - Reset your password");

            String body = "Dear " + username + ",\n\n" +
                    "We received a request to reset your MOH Connect password.\n\n" +
                    "Open this link to choose a new password:\n" +
                    resetLink + "\n\n" +
                    "The link works once and expires in " + validMinutes + " minutes.\n" +
                    "If you did not ask for this, you can ignore this email; your password stays the same.\n\n" +
                    "Best regards,\n" +
                    "MOH Connect Team";

            message.setText(body);
            mailSender.send(message);
            System.out.println("✓ Password reset link sent to " + toEmail);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send password reset email to " + toEmail + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Alert the PHM area's midwives that a mother sent an URGENT visit request.
     * @return true if the email was handed to the mail server, false if sending failed
     */
    public boolean sendUrgentVisitRequestAlert(List<String> toEmails, String motherName, String requestType,
                                               String reason, String contactNumber, String address) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmails.toArray(new String[0]));
            message.setSubject("URGENT: " + requestType + " request from " + motherName);

            String body = "An urgent request was sent through the MOH Connect Mother Portal.\n\n" +
                    "Mother: " + motherName + "\n" +
                    "Request: " + requestType + "\n" +
                    "Contact: " + (contactNumber != null ? contactNumber : "not given") + "\n" +
                    "Address: " + (address != null ? address : "not given") + "\n\n" +
                    "Reason:\n" + reason + "\n\n" +
                    "Please contact the mother as soon as possible and update the request in MOH Connect " +
                    "(Visit Requests page).\n\n" +
                    "MOH Connect";

            message.setText(body);
            mailSender.send(message);
            System.out.println("✓ Urgent visit request alert sent to " + toEmails);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send urgent visit request alert to " + toEmails + ": " + e.getMessage());
            return false;
        }
    }
}
