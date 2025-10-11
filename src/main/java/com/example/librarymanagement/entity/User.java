package com.example.librarymanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER) // fetch = FetchType.EAGER: khi load User, JPA tự động load luôn Role.
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /* Trong UserProfile, cột user_id là foreign key trỏ về bảng users.id.
    Cột user_id nằm ở bảng user_profile (nên UserProfile là bên sở hữu mối quan hệ, gọi là owning side).
    mappedBy = "user": Cột user bên trong class UserProfile mới là bên điều khiển mối quan hệ.*/
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile userProfile;

    // Chạy trước khi entity được lưu mới (insert).
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // Chạy trước khi entity được cập nhật (update).
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum UserStatus {
        ACTIVE, LOCKED, PENDING
    }
}
























































