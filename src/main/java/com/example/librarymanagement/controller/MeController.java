package com.example.librarymanagement.controller;

import com.example.librarymanagement.dto.ApiResponse;
import com.example.librarymanagement.dto.user.request.EmailChangeRequest;
import com.example.librarymanagement.dto.user.response.UserResponse;
import com.example.librarymanagement.service.inter.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class MeController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> getMe(Authentication authentication) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", user));
    }

    @PostMapping("/email-change")
    public ResponseEntity<ApiResponse<Void>> requestEmailChange(@Valid @RequestBody EmailChangeRequest req,
                                                                Authentication authentication) {
        userService.requestEmailChange(req, authentication);
        return ResponseEntity.ok(ApiResponse.success("Please check your email verification."));
    }

    @PostMapping("/email-change/resend")
    public ResponseEntity<ApiResponse<Void>> resendRequestEmailChange(@Valid @RequestBody EmailChangeRequest req,
                                                                      Authentication authentication) {
        userService.resendRequestEmailChange(req, authentication);
        return ResponseEntity.ok(ApiResponse.success("Please check your email verification."));
    }

    @GetMapping("/email-change/verify")
    public ResponseEntity<ApiResponse<Void>> confirmEmailChange(@RequestParam String token) {
        userService.confirmEmailChange(token);
        return ResponseEntity.ok(ApiResponse.success("Email update successfully"));
    }
}
