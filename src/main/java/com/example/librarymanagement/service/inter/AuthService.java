package com.example.librarymanagement.service.inter;

import com.example.librarymanagement.dto.auth.request.*;
import com.example.librarymanagement.dto.auth.response.JwtResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    void signup(SignupRequest req);

    void resendEmailSignup(ResendEmailSignupRequest req);

    void resendEmailForgotPassword(ForgotPasswordRequest req);

    void logout(String token, HttpServletRequest req, HttpServletResponse res);

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);

    JwtResponse login(LoginRequest req, HttpServletResponse res);

//    JwtResponse loginWithGoogle(GoogleAuthRequest req, HttpServletResponse res);

    JwtResponse refreshToken(RefreshTokenRequest refreshTokenRequest,
                             HttpServletRequest req,
                             HttpServletResponse res);

    void verifyEmail(String token);
}
