package com.example.librarymanagement.service.inter;

import com.example.librarymanagement.dto.auth.request.ForgotPasswordRequest;
import com.example.librarymanagement.dto.auth.request.LoginRequest;
import com.example.librarymanagement.dto.auth.request.ResendEmailSignupRequest;
import com.example.librarymanagement.dto.auth.request.SignupRequest;
import com.example.librarymanagement.dto.auth.response.JwtResponse;
import com.example.librarymanagement.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    public void signup(SignupRequest req);

    public void resendEmailSignup(ResendEmailSignupRequest req);

    public void resendEmailForgotPassword(ForgotPasswordRequest req);

    public void logout(String token, HttpServletRequest req, HttpServletResponse res);

    public void forgotPassword(String email);

    public void resetPassword(String token, String newPassword);

    public JwtResponse login(LoginRequest req, HttpServletResponse res);

    public JwtResponse refreshToken(HttpServletRequest req, HttpServletResponse res);

    public void verifyEmail(String token);

    public void saveRefreshToken(User user, String refreshToken);
}
