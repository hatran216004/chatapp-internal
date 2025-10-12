package com.example.librarymanagement.service;

import com.example.librarymanagement.dto.auth.request.LoginRequest;
import com.example.librarymanagement.dto.auth.request.SignupRequest;
import com.example.librarymanagement.dto.auth.response.JwtResponse;
import com.example.librarymanagement.entity.Role;
import com.example.librarymanagement.entity.User;
import com.example.librarymanagement.entity.UserProfile;
import com.example.librarymanagement.entity.VerificationToken;
import com.example.librarymanagement.exception.BadRequestException;
import com.example.librarymanagement.exception.UnauthorizedException;
import com.example.librarymanagement.repository.RoleRepository;
import com.example.librarymanagement.repository.UserRepository;
import com.example.librarymanagement.repository.VerificationTokenRepository;
import com.example.librarymanagement.security.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EmailTokenService emailTokenService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
//    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void signup(SignupRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("User Role not found"));

        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(userRole)
                .isEmailVerified(false)
                .status(User.UserStatus.ACTIVE).build();

        user = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(user)
                .fullName(req.getFullName())
                .build();

        user.setUserProfile(profile);
        userRepository.save(user);

        // Sent mail
        String verifitoken = emailTokenService.createVerificationToken(
                user,
                VerificationToken.TokenPurpose.VERIFY_EMAIL);
        emailService.sendVerificationEmail(user.getEmail(), verifitoken);
    }

    @Transactional
    public JwtResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password") {
                });

        if (!user.getIsEmailVerified()) {
            throw new UnauthorizedException("Please verify your email before logging in");
        }

        if (user.getStatus() == User.UserStatus.LOCKED) {
            throw new UnauthorizedException("Account is locked");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

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
    public void verifyEmail(String token) {
        VerificationToken verificationToken = emailTokenService.validateToken(
                token,
                VerificationToken.TokenPurpose.VERIFY_EMAIL);

        User user = verificationToken.getUser();
        user.setIsEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationToken.setUsedAt(LocalDateTime.now());

        verificationTokenRepository.save(verificationToken);
    }
}














































