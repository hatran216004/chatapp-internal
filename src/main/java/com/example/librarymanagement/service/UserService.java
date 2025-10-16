package com.example.librarymanagement.service;

import com.example.librarymanagement.dto.user.request.CreateUserRequest;
import com.example.librarymanagement.dto.user.request.EmailChangeRequest;
import com.example.librarymanagement.dto.user.request.UpdateUserRequest;
import com.example.librarymanagement.dto.user.response.UserResponse;
import com.example.librarymanagement.dto.util.PageResponse;
import com.example.librarymanagement.entity.Role;
import com.example.librarymanagement.entity.User;
import com.example.librarymanagement.entity.UserProfile;
import com.example.librarymanagement.entity.VerificationToken;
import com.example.librarymanagement.exception.BadRequestException;
import com.example.librarymanagement.exception.ResourceNotFoundException;
import com.example.librarymanagement.exception.UnauthorizedException;
import com.example.librarymanagement.repository.RoleRepository;
import com.example.librarymanagement.repository.UserProfileRepository;
import com.example.librarymanagement.repository.UserRepository;
import com.example.librarymanagement.repository.VerificationTokenRepository;
import com.example.librarymanagement.security.service.UserDetailsImpl;
import com.example.librarymanagement.util.TokenHashUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.SendFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileRepository userProfileRepository;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;
    private final EmailTokenService emailTokenService;
    private final TokenHashUtil tokenHashUtil;
    private final VerificationTokenRepository verificationTokenRepository;

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        Page<UserResponse> page = userRepository.findAll(pageable)
                .map(this::mapToUserResponse);
        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest req) {
        String email = req.getEmail();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already registered");
        }

        String roleName = req.getRoleName();
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new BadRequestException("Role not found: " + roleName));

        User user = User.builder()
                .email(email)
                .role(role)
                .status(User.UserStatus.ACTIVE)
                .password(passwordEncoder.encode(req.getPassword()))
                .isEmailVerified(true)
                .build();

        userRepository.save(user);

        UserProfile userProfile = UserProfile.builder()
                .user(user)
                .fullName(req.getFullName())
                .address(req.getAddress())
                .phone(req.getPhone())
                .dob(req.getDob())
                .gender(req.getGender())
                .build();

        userProfileRepository.save(userProfile);

        user.setUserProfile(userProfile);
        userRepository.save(user);

        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateUserById(Integer userId, UpdateUserRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        String roleName = req.getRoleName();
        if (roleName != null) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new BadRequestException("Role not found: " + roleName));
            user.setRole(role);
        }

        if (req.getStatus() != null) {
            user.setStatus(req.getStatus());
        }

        // Profile
        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            profile = UserProfile.builder().user(user).build();
        }

        if (req.getAddress() != null) {
            profile.setAddress(req.getAddress());
        }
        if (req.getFullName() != null) {
            profile.setFullName(req.getFullName());
        }
        if (req.getDob() != null) {
            profile.setDob(req.getDob());
        }
        if (req.getPhone() != null) {
            profile.setPhone(req.getPhone());
        }
        if (req.getGender() != null) {
            profile.setGender(req.getGender());
        }

        userProfileRepository.save(profile);
        user.setUserProfile(profile);
        user = userRepository.save(user);

        return mapToUserResponse(user);
    }

    @Transactional
    public void deleteUserById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        userRepository.delete(user);
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToUserResponse(user);
    }

    @Transactional
    public void requestEmailChange(EmailChangeRequest req) {
        String newEmail = req.getEmail();

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BadRequestException("User not found with ID: " + userDetails.getId()));

        String changeEmailToken = emailTokenService.createVerificationTokenChangeEmail(user,
                newEmail);
        try {
            emailService.sendVerificationEmail(newEmail, changeEmailToken);
        } catch (SendFailedException ex) {
            throw new RuntimeException("There was an error sending the email. Try again later!");
        }
    }

    @Transactional
    public void confirmEmailChange(String token) {
        VerificationToken verificationToken = emailTokenService.validateToken(token,
                VerificationToken.TokenPurpose.CHANGE_EMAIL);

        verificationToken.setUsedAt(System.currentTimeMillis());
        verificationToken.setUsed(true);
        verificationToken.setNewEmail(null);
        verificationTokenRepository.save(verificationToken);

        String newEmail = verificationToken.getNewEmail();
        User user = userRepository.findById(verificationToken.getUser().getId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        user.setEmail(newEmail);
        userRepository.save(user);
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