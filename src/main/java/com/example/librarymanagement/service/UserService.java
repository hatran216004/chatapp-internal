package com.example.librarymanagement.service;

import com.example.librarymanagement.dto.user.response.UserResponse;
import com.example.librarymanagement.dto.util.PageResponse;
import com.example.librarymanagement.entity.User;
import com.example.librarymanagement.entity.UserProfile;
import com.example.librarymanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        Page<UserResponse> page = userRepository.findAll(pageable)
                .map(this::mapToUserResponse);
        return PageResponse.from(page);

    }

    private UserResponse mapToUserResponse(User user) {
        UserProfile userProfile = user.getUserProfile();

        String gender = userProfile.getGender() != null ? userProfile.getGender().name() : null;
        String phone = userProfile.getPhone() != null ? userProfile.getPhone() : null;
        String address = userProfile.getAddress() != null ? userProfile.getAddress() : null;
        LocalDate dob = userProfile.getDob() != null ? userProfile.getDob() : null;

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .roleName(user.getRole().getName())
                .isEmailVerified(user.getIsEmailVerified())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .fullName(userProfile.getFullName())
                .dob(dob)
                .gender(gender)
                .phone(phone)
                .address(address)
                .build();
    }
}
