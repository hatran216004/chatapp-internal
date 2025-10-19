package com.example.librarymanagement.service.inter;

import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

public interface S3Service {
    String uploadFile(MultipartFile file, String folderPath);

    byte[] downloadFile(String key);

    String generatePresignedUrl(String key, Duration duration);
}
