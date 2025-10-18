package com.example.librarymanagement.service.inter;

import jakarta.mail.SendFailedException;

public interface EmailService {
    void sendVerificationEmail(String toEmail, String token) throws SendFailedException;
}
