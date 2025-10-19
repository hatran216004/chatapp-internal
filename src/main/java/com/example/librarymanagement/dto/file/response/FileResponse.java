package com.example.librarymanagement.dto.file.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileResponse {
    private String key;
    private String url;
    private String fileName;
    private String contentType;
    private Long size;
    private LocalDateTime uploadedAt;
}
