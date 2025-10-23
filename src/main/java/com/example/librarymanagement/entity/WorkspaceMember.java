package com.example.librarymanagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workspace_members")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private MemberRole role = MemberRole.MEMBER;

    @Column(name = "joined_at")
    private Long joinedAt;

    @PrePersist
    protected void onCreate() {
        joinedAt = System.currentTimeMillis();
    }

    public enum MemberRole {ADMIN, MEMBER, GUEST}
}
