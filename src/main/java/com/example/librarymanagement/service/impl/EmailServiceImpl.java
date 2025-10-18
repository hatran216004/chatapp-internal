package com.example.librarymanagement.service.impl;

import com.example.librarymanagement.service.inter.EmailService;
import jakarta.mail.SendFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.verify-email-url}")
    private String verifyEmailUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String token) throws SendFailedException {
        String verificationLink = verifyEmailUrl + "?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Verify your email address");
        message.setText("Please click the link below to verify your email address:\n\n" +
                verificationLink + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you didn't create an account, please ignore this email.");

        mailSender.send(message);
    }
}


























































