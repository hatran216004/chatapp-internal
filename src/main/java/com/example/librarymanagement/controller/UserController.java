package com.example.librarymanagement.controller;

import com.example.librarymanagement.dto.ApiResponse;
import com.example.librarymanagement.dto.user.request.CreateUserRequest;
import com.example.librarymanagement.dto.user.request.UpdateUserRequest;
import com.example.librarymanagement.dto.user.response.UserResponse;
import com.example.librarymanagement.dto.util.PageResponse;
import com.example.librarymanagement.service.inter.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<UserResponse> users = userService.getAllUsers(pageable);

        return ResponseEntity.ok(ApiResponse.success("successfully", users));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Integer userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success("successfully", user));
    }

    @PostMapping()
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest req) {
        UserResponse user = userService.createUser(req);
        return ResponseEntity.ok(ApiResponse.success("successfully", user));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Integer userId,
                                                                @Valid @RequestBody UpdateUserRequest req) {
        UserResponse userUpdated = userService.updateUserById(userId, req);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", userUpdated));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Integer userId,
                                                        Authentication authentication) {
        userService.deleteUserById(userId, authentication);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }
}









