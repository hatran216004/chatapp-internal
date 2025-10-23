package com.example.librarymanagement.dto.chat;

public class MessageRead {
    private Integer userId; // ID của người dùng đã đọc tin nhắn
    private Integer conversationId;
    private Integer lastMessageId;
    private Long readAt;
}
