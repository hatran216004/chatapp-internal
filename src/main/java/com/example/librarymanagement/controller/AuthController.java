package com.example.librarymanagement.controller;

import com.example.librarymanagement.dto.ApiResponse;
import com.example.librarymanagement.dto.auth.request.*;
import com.example.librarymanagement.dto.auth.response.JwtResponse;
import com.example.librarymanagement.service.inter.AuthService;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest req) {
        authService.signup(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successfully. Please check your email to verify your account."));
    }

    @PostMapping("/signup/resend")
    public ResponseEntity<ApiResponse<Void>> resendSignupVerification(@Valid @RequestBody ResendEmailSignupRequest req) {
        authService.resendEmailSignup(req);
        return ResponseEntity.ok(ApiResponse
                .success("If this email is registered, a confirmation link has been sent."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest req,
                                                          HttpServletResponse res) {
        JwtResponse data = authService.login(req, res);
        return ResponseEntity.ok(ApiResponse.success("Login successfully.", data));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader,
                                                    HttpServletRequest req,
                                                    HttpServletResponse res) {
        String accessToken = authHeader.substring(7);
        authService.logout(accessToken, req, res);
        return ResponseEntity.ok(ApiResponse.success("Logout successfully."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        String email = req.getEmail();
        authService.forgotPassword(email);
        return ResponseEntity.ok(ApiResponse.success("Please check your email to verify password reset token."));
    }

    @PostMapping("/forgot-password/resend")
    public ResponseEntity<ApiResponse<Void>> resendEmailForgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.resendEmailForgotPassword(req);
        return ResponseEntity.ok(ApiResponse.success("Please check your email to verify password reset token."));
    }

    @PutMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req,
                                                           @RequestParam String token) {
        authService.resetPassword(token, req.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Reset password successfully."));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<JwtResponse>> refreshToken(
            @Nullable @RequestBody RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest req,
            HttpServletResponse res) {
        JwtResponse data = authService.refreshToken(refreshTokenRequest, req, res);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully.", data));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully. You can now login."));
    }
}