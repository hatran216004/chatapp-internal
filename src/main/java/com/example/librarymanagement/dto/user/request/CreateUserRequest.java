package com.example.librarymanagement.dto.user.request;

import com.example.librarymanagement.entity.User;
import com.example.librarymanagement.entity.UserProfile;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
            message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character"
    )
    private String password;

    @NotBlank(message = "Role name is required")
    private String roleName;

    private User.UserStatus status;

    private String fullName;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;

    private UserProfile.Gender gender;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Invalid phone number format")
    private String phone;

    private String address;
}
