package com.example.librarymanagement.service;

import com.example.librarymanagement.entity.User;
import com.example.librarymanagement.entity.VerificationToken;
import com.example.librarymanagement.exception.BadRequestException;
import com.example.librarymanagement.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailTokenService {
    private final VerificationTokenRepository verificationTokenRepository;

    @Transactional
    public String createVerificationToken(User user, VerificationToken.TokenPurpose purpose) {
        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = VerificationToken.builder()
                .purpose(purpose)
                .token(token)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();

        verificationTokenRepository.save(verificationToken);

        return token;
    }

    @Transactional(readOnly = true)
    public VerificationToken validateToken(String token, VerificationToken.TokenPurpose purpose) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid verification token"));

        if (verificationToken.getPurpose() != purpose) {
            throw new BadRequestException("Token purpose mismatch");
        }

        if (verificationToken.getUsed()) {
            throw new BadRequestException("Token has already been used");
        }

        if (verificationToken.isExpired()) {
            throw new BadRequestException("Token has expired");
        }

        return verificationToken;
    }

    @Transactional
    public void cleanupExpiredTokens() {
        verificationTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}






















