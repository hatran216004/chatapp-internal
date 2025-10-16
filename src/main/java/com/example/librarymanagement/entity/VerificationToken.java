package com.example.librarymanagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "verification_tokens")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false)
    private TokenPurpose purpose;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "new_email")
    private String newEmail;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "expires_at", nullable = false)
    private Long expiresAt;

    @Column(name = "used")
    private Boolean used = false;

    @Column(name = "used_at")
    private Long usedAt;

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }

    public enum TokenPurpose {VERIFY_EMAIL, CHANGE_EMAIL, RESET_PASSWORD}
}














































