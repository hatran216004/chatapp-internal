package com.example.librarymanagement.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TypingIndicator {
    private Integer userId;
    private Integer conversationId;
    private Boolean typing;
}
