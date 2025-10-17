package com.example.librarymanagement.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResendEmailSignupRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    private String email;
}
