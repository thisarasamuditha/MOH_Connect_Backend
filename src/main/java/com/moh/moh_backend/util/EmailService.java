package com.moh.moh_backend.util;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

/**
 * Email service for sending styled HTML notifications via Thymeleaf templates.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.from:no-reply@moh.gov.lk}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Send mother credentials to email using the Thymeleaf template.
     **/
    public boolean sendMotherCredentials(String toEmail, String username, String password, String motherName, String mohArea) {
        try {
            Context context = new Context();
            context.setVariable("motherName", motherName != null ? motherName : "Mother");
            context.setVariable("mohArea", mohArea != null ? mohArea : "");
            context.setVariable("username", username != null ? username : "");
            context.setVariable("password", password != null ? password : "");
            context.setVariable("email", toEmail!= null ? toEmail : "");

            String htmlBody = templateEngine.process("email/mother-credentials", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("MOH Connect - Your Account Credentials");
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);
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
            Context context = new Context();
            context.setVariable("username", username != null ? username : "User");
            context.setVariable("resetLink", resetLink != null ? resetLink : "#");
            context.setVariable("validMinutes", validMinutes);

            String htmlBody = templateEngine.process("email/password-reset", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("MOH Connect - Reset your password");
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);
            System.out.println("✓ Password reset link sent to " + toEmail);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send password reset email to " + toEmail + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Alert the PHM area's midwives that a mother sent an URGENT visit request.
     **/
    public boolean sendUrgentVisitRequestAlert(List<String> toEmails, String motherName, String requestType,
                                               String reason, String contactNumber, String address) {
        if (toEmails == null || toEmails.isEmpty()) {
            return false;
        }
        try {
            Context context = new Context();
            context.setVariable("motherName", motherName != null ? motherName : "Mother");
            context.setVariable("requestType", requestType != null ? requestType : "Visit Request");
            context.setVariable("reason", reason != null ? reason : "No reason provided");
            context.setVariable("contactNumber", contactNumber != null && !contactNumber.isBlank() ? contactNumber : "Not provided");
            context.setVariable("address", address != null && !address.isBlank() ? address : "Not provided");

            String htmlBody = templateEngine.process("email/urgent-visit-alert", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmails.toArray(new String[0]));
            helper.setSubject("URGENT: " + requestType + " request from " + motherName);
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);
            System.out.println("✓ Urgent visit request alert sent to " + toEmails);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send urgent visit request alert to " + toEmails + ": " + e.getMessage());
            return false;
        }
    }
}
