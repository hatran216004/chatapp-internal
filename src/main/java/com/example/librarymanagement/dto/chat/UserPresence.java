package com.example.librarymanagement.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
// Thông tin trạng thái trực tuyến (online/offline) của người dùng
public class UserPresence {
    private Integer userId;
    private Boolean online;
    private Long timestamp;
}
