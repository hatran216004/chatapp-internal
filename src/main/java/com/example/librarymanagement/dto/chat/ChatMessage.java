package com.example.librarymanagement.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private Integer id;
    private Integer conversationId;
    private Integer sender;
    private Integer parentId; // Dùng nếu có tin nhắn trả lời (reply) → ID của tin nhắn gốc
    private String content;
    private String senderName;
    private String senderAvatar;
    private Long createdAt;
    private Long updatedAt;
}
