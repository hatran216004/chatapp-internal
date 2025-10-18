package com.example.librarymanagement.service.impl;

import com.example.librarymanagement.dto.auth.request.*;
import com.example.librarymanagement.dto.auth.response.JwtResponse;
import com.example.librarymanagement.entity.*;
import com.example.librarymanagement.exception.BadRequestException;
import com.example.librarymanagement.exception.UnauthorizedException;
import com.example.librarymanagement.repository.*;
import com.example.librarymanagement.security.service.UserDetailsServiceImpl;
import com.example.librarymanagement.security.util.JwtTokenProvider;
import com.example.librarymanagement.service.inter.AuthService;
import com.example.librarymanagement.service.inter.EmailService;
import com.example.librarymanagement.service.inter.EmailTokenService;
import com.example.librarymanagement.util.CookieUtil;
import com.example.librarymanagement.util.TokenHashUtil;
import jakarta.mail.SendFailedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtBlacklistRepository jwtBlacklistRepository;
    private final EmailService emailService;
    private final EmailTokenService emailTokenService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;
    private final TokenHashUtil tokenHashUtil;

    @Transactional
    public void signup(SignupRequest req) {
        String email = req.getEmail();

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("User Role not found"));

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(req.getPassword()))
                .role(userRole)
                .isEmailVerified(false)
                .status(User.UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(user)
                .build();

        user.setUserProfile(profile);
        userRepository.save(user);

        // Sent mail
        String verifyToken = emailTokenService.createVerificationToken(
                user,
                VerificationToken.TokenPurpose.VERIFY_EMAIL);

        try {
            emailService.sendVerificationEmail(user.getEmail(), verifyToken);
        } catch (SendFailedException ex) {
            throw new RuntimeException("There was an error sending the email. Try again later!");
        }
    }

    @Transactional
    public void resendEmailSignup(ResendEmailSignupRequest req) {
        String email = req.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        verificationTokenRepository.deleteByUserIdAndPurpose(user.getId(),
                VerificationToken.TokenPurpose.VERIFY_EMAIL);

        String newVerificationToken = emailTokenService.createVerificationToken(user,
                VerificationToken.TokenPurpose.VERIFY_EMAIL);
        try {
            emailService.sendVerificationEmail(email, newVerificationToken);
        } catch (SendFailedException ex) {
            throw new RuntimeException("There was an error sending the email. Try again later!");
        }
    }

    @Transactional
    public void resendEmailForgotPassword(ForgotPasswordRequest req) {
        String email = req.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        verificationTokenRepository.deleteByUserIdAndPurpose(user.getId(),
                VerificationToken.TokenPurpose.RESET_PASSWORD);

        String newForgotPasswordToken = emailTokenService.createVerificationToken(user,
                VerificationToken.TokenPurpose.RESET_PASSWORD);
        try {
            emailService.sendVerificationEmail(email, newForgotPasswordToken);
        } catch (SendFailedException ex) {
            throw new RuntimeException("There was an error sending the email. Try again later!");
        }
    }

    @Transactional
    public JwtResponse login(LoginRequest req, HttpServletResponse res) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password") {
                });

        if (!user.getIsEmailVerified()) {
            throw new UnauthorizedException("Please verify your email before logging in");
        }

        if (user.getStatus() == User.UserStatus.LOCKED) {
            throw new UnauthorizedException("Account is locked");
        }

        Authentication authToken = new UsernamePasswordAuthenticationToken(
                req.getEmail(),
                req.getPassword());
        Authentication authentication = authenticationManager.authenticate(authToken);

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // Save refresh token to db
        saveRefreshToken(user, refreshToken);

        cookieUtil.addRefreshTokenCookie(res, refreshToken);

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .build();
    }

    @Transactional
    public void logout(String token, HttpServletRequest req, HttpServletResponse res) {
        String jti = jwtTokenProvider.extractJti(token, JwtTokenProvider.TokenKind.ACCESS);
        Integer userId = Integer.parseInt(jwtTokenProvider.extractSubject(token,
                JwtTokenProvider.TokenKind.ACCESS));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        long issuedAt = jwtTokenProvider.extractIssueAt(token,
                JwtTokenProvider.TokenKind.ACCESS).getTime();
        Long expiresAt = issuedAt + jwtTokenProvider.getAccessTokenExpirationMs();

        // Add to blacklist
        JwtBlacklist blacklist = JwtBlacklist.builder()
                .user(user)
                .revoked(true)
                .tokenJti(jti)
                .expiresAt(expiresAt)
                .build();

        jwtBlacklistRepository.save(blacklist);

        // Revoke refresh token
        String refreshTokenFromCookie = cookieUtil.getRefreshTokenFromCookie(req)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found"));

        String refreshTokenHash = tokenHashUtil.hashToken(refreshTokenFromCookie);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(refreshTokenHash)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        // Xóa refresh token cookie
        cookieUtil.deleteRefreshTokenFromCookie(res);

        SecurityContextHolder.clearContext();
    }

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) return;

        String passwordResetToken = emailTokenService.createVerificationToken(user,
                VerificationToken.TokenPurpose.RESET_PASSWORD);
        try {
            emailService.sendVerificationEmail(user.getEmail(), passwordResetToken);

        } catch (SendFailedException ex) {
            throw new RuntimeException("There was an error sending the email. Try again later!");
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        VerificationToken verificationToken = emailTokenService.validateToken(token,
                VerificationToken.TokenPurpose.RESET_PASSWORD);

        verificationToken.setUsed(true);
        verificationToken.setUsedAt(System.currentTimeMillis());
        verificationTokenRepository.save(verificationToken);

        User user = userRepository.findById(verificationToken.getUser().getId())
                .orElseThrow(() -> new UnauthorizedException("User not found with reset password token"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public JwtResponse refreshToken(RefreshTokenRequest refreshTokenRequest,
                                    HttpServletRequest req,
                                    HttpServletResponse res) {
        // 1. Lấy token từ cookie / req body
        String refreshTokenString = cookieUtil.getRefreshTokenFromCookie(req)
                .orElseGet(() -> {
                    if (refreshTokenRequest == null
                            || refreshTokenRequest.getRefreshToken() == null
                            || refreshTokenRequest.getRefreshToken().isBlank()
                    ) {
                        return null;
                    }
                    return refreshTokenRequest.getRefreshToken();
                });

        if (refreshTokenString == null) {
            throw new UnauthorizedException("Refresh token not found in cookie/body");
        }

        // 2. Validate token
        if (!jwtTokenProvider.validateRefreshToken(refreshTokenString)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        // 3.Hash token để tìm trong db
        String oldRefreshTokenHash = tokenHashUtil.hashToken(refreshTokenString);

        // 4. Tìm token trong db
        RefreshToken oldRefreshToken = refreshTokenRepository.findByTokenHash(oldRefreshTokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        // 5. Nếu token đã revoke -> Lỗi
        if (oldRefreshToken.getRevoked()) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }

        // 5. Nếu token expires -> Lỗi
        if (oldRefreshToken.isExpired()) {
            throw new UnauthorizedException("Refresh token has expired");
        }

        // 6. Tìm user tương ứng với token
        User user = oldRefreshToken.getUser();
        if (user == null) {
            throw new UnauthorizedException("User not found");
        }

        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(user.getEmail());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // 7. Generate new tokens
        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);


        // 8. Revoke old refresh token
        oldRefreshToken.setRevoked(true);
        oldRefreshToken.setReplacedBy(jwtTokenProvider.extractJti(newRefreshToken,
                JwtTokenProvider.TokenKind.REFRESH));
        refreshTokenRepository.save(oldRefreshToken);

        // 9. Save to db
        saveRefreshToken(user, newRefreshToken);

        // 10. set cookie
        cookieUtil.addRefreshTokenCookie(res, newRefreshToken);

        return JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .build();
    }

    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = emailTokenService.validateToken(
                token,
                VerificationToken.TokenPurpose.VERIFY_EMAIL);

        User user = verificationToken.getUser();
        user.setIsEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationToken.setUsedAt(System.currentTimeMillis());

        verificationTokenRepository.save(verificationToken);
    }

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