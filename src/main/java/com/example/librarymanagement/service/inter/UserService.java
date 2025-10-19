package com.example.librarymanagement.service.inter;

import com.example.librarymanagement.dto.user.request.CreateUserRequest;
import com.example.librarymanagement.dto.user.request.EmailChangeRequest;
import com.example.librarymanagement.dto.user.request.UpdateUserRequest;
import com.example.librarymanagement.dto.user.response.UserResponse;
import com.example.librarymanagement.dto.util.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface UserService {

    PageResponse<UserResponse> getAllUsers(Pageable pageable);

    UserResponse getUserById(Integer userId);

    UserResponse createUser(CreateUserRequest req);

    UserResponse updateUserById(Integer userId, UpdateUserRequest req);

    UserResponse getUserByEmail(String email);

    void deleteUserById(Integer userId, Authentication authentication);

    void requestEmailChange(EmailChangeRequest req, Authentication authentication);

    void resendRequestEmailChange(EmailChangeRequest req, Authentication authentication);

    void confirmEmailChange(String token);
}
