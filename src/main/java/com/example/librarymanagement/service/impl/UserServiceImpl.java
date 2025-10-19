package com.example.librarymanagement.service.impl;

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
import com.example.librarymanagement.service.inter.EmailService;
import com.example.librarymanagement.service.inter.EmailTokenService;
import com.example.librarymanagement.service.inter.S3Service;
import com.example.librarymanagement.service.inter.UserService;
import com.example.librarymanagement.util.TokenHashUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.SendFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileRepository userProfileRepository;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;
    private final EmailTokenService emailTokenService;
    private final TokenHashUtil tokenHashUtil;
    private final VerificationTokenRepository verificationTokenRepository;
    private final S3Service s3Service;

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
                .bio(req.getBio())
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
        if (req.getBio() != null) {
            profile.setBio(req.getBio());
        }

        userProfileRepository.save(profile);
        user.setUserProfile(profile);
        user = userRepository.save(user);

        return mapToUserResponse(user);
    }

    @Transactional
    public void deleteUserById(Integer userId, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (email.equalsIgnoreCase(user.getEmail())) {
            throw new BadRequestException("You cannot delete your own account.");
        }
        user.setDeletedAt(System.currentTimeMillis());

        userRepository.save(user);
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToUserResponse(user);
    }

    @Transactional
    public void requestEmailChange(EmailChangeRequest req, Authentication authentication) {
        String newEmail = req.getEmail();

        if (userRepository.existsByEmail(newEmail)) {
            throw new BadRequestException("Email already registered" + newEmail);
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found with email: " + email));

        String changeEmailToken = emailTokenService.createVerificationToken(
                user,
                VerificationToken.TokenPurpose.CHANGE_EMAIL,
                newEmail);
        try {
            emailService.sendVerificationEmail(newEmail, changeEmailToken);
        } catch (SendFailedException ex) {
            throw new RuntimeException("There was an error sending the email. Try again later!");
        }
    }

    @Transactional
    public void resendRequestEmailChange(EmailChangeRequest req, Authentication authentication) {
        String newEmail = req.getEmail();
        String currentEmail = authentication.getName();

        if (currentEmail.equalsIgnoreCase(newEmail)) {
            throw new BadRequestException("New email cannot be the same as current email");
        }

        if (userRepository.existsByEmail(newEmail)) {
            throw new BadRequestException("Email already registered" + newEmail);
        }
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new BadRequestException("User not found with email: " + currentEmail));

        // Always delete old tokens and create new one
        verificationTokenRepository.deleteByUserIdAndPurpose(user.getId(),
                VerificationToken.TokenPurpose.CHANGE_EMAIL);

        String newVerificationToken = emailTokenService.createVerificationToken(user,
                VerificationToken.TokenPurpose.CHANGE_EMAIL,
                newEmail);
        try {
            emailService.sendVerificationEmail(newEmail, newVerificationToken);
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
        String fullname = userProfile.getFullName() != null ? userProfile.getFullName() : null;
        String phone = userProfile.getPhone() != null ? userProfile.getPhone() : null;
        String address = userProfile.getAddress() != null ? userProfile.getAddress() : null;
        LocalDate dob = userProfile.getDob() != null ? userProfile.getDob() : null;

        String avatarUrl = s3Service.generatePresignedUrl(userProfile.getAvatarS3Key(), Duration.ofHours(24));

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .roleName(user.getRole().getName())
                .isEmailVerified(user.getIsEmailVerified())
                .status(user.getStatus().name())
                .deletedAt(user.getDeletedAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .avatarUrl(avatarUrl)
                .fullName(fullname)
                .dob(dob)
                .gender(gender)
                .phone(phone)
                .address(address)
                .build();
    }
}