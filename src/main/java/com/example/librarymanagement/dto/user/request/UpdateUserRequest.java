package com.example.librarymanagement.dto.user.request;

import com.example.librarymanagement.entity.User;
import com.example.librarymanagement.entity.UserProfile;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
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
