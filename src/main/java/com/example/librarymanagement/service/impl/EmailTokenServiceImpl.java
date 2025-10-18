package com.example.librarymanagement.service.impl;

import com.example.librarymanagement.entity.User;
import com.example.librarymanagement.entity.VerificationToken;
import com.example.librarymanagement.exception.BadRequestException;
import com.example.librarymanagement.repository.VerificationTokenRepository;
import com.example.librarymanagement.service.inter.EmailTokenService;
import com.example.librarymanagement.util.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailTokenServiceImpl implements EmailTokenService {
    private final VerificationTokenRepository verificationTokenRepository;
    private final TokenHashUtil tokenHashUtil;

    @Value("${verify.token.mail}")
    private Long verifySignupTokenExpirationMs;

    @Value("${verify.token.reset-password}")
    private Long resetPasswordTokenExpirationMs;

    @Value("${verify.token.change-email}")
    private Long changeEmailTokenExpirationMs;

    @Transactional
    public String createVerificationToken(User user, VerificationToken.TokenPurpose purpose) {
        String token = UUID.randomUUID().toString();

        Long now = System.currentTimeMillis();
        Long expiresAt = now + (purpose == VerificationToken.TokenPurpose.VERIFY_EMAIL
                ? verifySignupTokenExpirationMs
                : purpose == VerificationToken.TokenPurpose.RESET_PASSWORD
                ? resetPasswordTokenExpirationMs
                : changeEmailTokenExpirationMs);

        VerificationToken verificationToken = VerificationToken.builder()
                .purpose(purpose)
                .token(purpose == VerificationToken.TokenPurpose.VERIFY_EMAIL
                        ? token
                        : tokenHashUtil.hashToken(token))
                .user(user)
                .createdAt(now)
                .expiresAt(expiresAt)
                .used(false)
                .build();

        verificationTokenRepository.save(verificationToken);

        return token;
    }

    @Transactional
    public String createVerificationToken(User user,
                                          VerificationToken.TokenPurpose purpose,
                                          String newEmail) {
        String token = UUID.randomUUID().toString();

        Long now = System.currentTimeMillis();
        Long expiresAt = now + changeEmailTokenExpirationMs;

        VerificationToken verificationToken = VerificationToken.builder()
                .purpose(VerificationToken.TokenPurpose.CHANGE_EMAIL)
                .token(tokenHashUtil.hashToken(token))
                .user(user)
                .createdAt(now)
                .newEmail(newEmail)
                .expiresAt(expiresAt)
                .used(false)
                .build();

        verificationTokenRepository.save(verificationToken);

        return token;
    }

    @Transactional(readOnly = true)
    public VerificationToken validateToken(String token, VerificationToken.TokenPurpose purpose) {
        String tokenString = purpose == VerificationToken.TokenPurpose.VERIFY_EMAIL
                ? token
                : tokenHashUtil.hashToken(token);
        VerificationToken verificationToken = verificationTokenRepository.findByToken(tokenString)
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
        verificationTokenRepository.deleteExpiredTokens(System.currentTimeMillis());
    }
}






















