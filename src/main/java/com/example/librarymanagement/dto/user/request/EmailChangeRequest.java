package com.example.librarymanagement.dto.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailChangeRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    private String email;
}
