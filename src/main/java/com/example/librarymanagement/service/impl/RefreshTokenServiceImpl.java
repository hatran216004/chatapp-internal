package com.example.librarymanagement.service.impl;

import com.example.librarymanagement.entity.RefreshToken;
import com.example.librarymanagement.entity.User;
import com.example.librarymanagement.repository.RefreshTokenRepository;
import com.example.librarymanagement.security.util.JwtTokenProvider;
import com.example.librarymanagement.service.inter.RefreshTokenService;
import com.example.librarymanagement.util.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenHashUtil tokenHashUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public void saveRefreshToken(User user, String refreshToken) {
        Long refreshTTL = jwtTokenProvider.getRefreshTokenExpirationMs();
        Long refreshIssueAt = jwtTokenProvider.extractIssueAt(refreshToken,
                JwtTokenProvider.TokenKind.REFRESH).getTime();
        Long expiresAt = refreshIssueAt + refreshTTL;

        RefreshToken newRt = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHashUtil.hashToken(refreshToken))
                .issuedAt(refreshIssueAt)
                .revoked(false)
                .expiresAt(expiresAt)
                .replacedBy(null)
                .build();

        refreshTokenRepository.save(newRt);
    }
}
