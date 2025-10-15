package com.example.librarymanagement.dto.user.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Integer id;
    private String email;
    private String roleName;
    private Boolean isEmailVerified;
    private String status;

    private String fullName;
    private LocalDate dob;
    private String gender;
    private String phone;
    private String address;

    private Long createdAt;
    private Long updatedAt;
}
