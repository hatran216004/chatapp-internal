package com.example.librarymanagement.controller;

import com.example.librarymanagement.dto.ApiResponse;
import com.example.librarymanagement.dto.user.request.EmailChangeRequest;
import com.example.librarymanagement.dto.user.response.UserResponse;
import com.example.librarymanagement.security.service.UserDetailsImpl;
import com.example.librarymanagement.service.UserService;
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
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        UserResponse user = userService.getUserByEmail(userDetails.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", user));
    }

    @PostMapping("/email-change")
    public ResponseEntity<ApiResponse<Void>> requestEmailChange(@Valid @RequestBody EmailChangeRequest req) {
        userService.requestEmailChange(req);
        return ResponseEntity.ok(ApiResponse.success("Please check your email verification."));
    }

    @GetMapping("/email-change/verify")
    public ResponseEntity<ApiResponse<Void>> confirmEmailChange(@RequestParam String token) {
        userService.confirmEmailChange(token);
        return ResponseEntity.ok(ApiResponse.success("Email update successfully"));
    }
}
