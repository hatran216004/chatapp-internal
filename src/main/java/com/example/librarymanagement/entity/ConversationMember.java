package com.example.librarymanagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "conversation_members")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private ConversationMemberRole role;

    @Column(name = "joined_at")
    private Long joinedAt;

    @Column(name = "last_read_message_id")
    private Integer lastReadMessageId;

    @Column(name = "last_read_at")
    private Long lastReadAt;

    @Column(name = "is_notif_enabled")
    private Boolean isNotifEnabled;

    enum ConversationMemberRole {
        ADMIN, MEMBER, GUEST
    }

    @PrePersist
    protected void onCreate() {
        joinedAt = System.currentTimeMillis();
    }

}
