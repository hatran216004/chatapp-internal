package com.example.librarymanagement.service.impl;

import com.example.librarymanagement.config.AwsBuckets;
import com.example.librarymanagement.service.inter.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ServiceImpl implements S3Service {
    private final S3Client s3Client;
    private final AwsBuckets awsBuckets;

    @Override
    public String uploadFile(MultipartFile file, String folderPath) {
        String filename = generateUniqueFilename(file.getOriginalFilename());
        String s3Key = folderPath + filename;

        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));

        try {
            putObjectRequest(s3Key, file, metadata);

            log.info("File uploaded successfully: {}", s3Key);

            return s3Key;
        } catch (IOException ex) {
            throw new RuntimeException("");
        }
    }

    public String generatePresignedUrl(String key, Duration duration) {
        try (S3Presigner s3Presigner = S3Presigner.create()) {
            GetObjectRequest objectRequest = getObjectRequest(key);

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .getObjectRequest(objectRequest)
                    .build();
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

            return presignedRequest.url().toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] downloadFile(String key) {
        try {
            GetObjectRequest objectRequest = getObjectRequest(key);
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(objectRequest);

            return response.readAllBytes();
        } catch (IOException ex) {
            log.error("Error downloading file: {}", key, ex);
            throw new RuntimeException("Error downloading file", ex);
        }
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            int dotIndex = originalFilename.lastIndexOf(".");
            extension = originalFilename.substring(dotIndex);
        }
        return UUID.randomUUID().toString() + extension;
    }

    private void putObjectRequest(String key,
                                  MultipartFile file,
                                  Map<String, String> metadata) throws IOException {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(awsBuckets.getMain())
                .key(key)
                .contentType(file.getContentType())
                .metadata(metadata)
                .build();

        s3Client.putObject(objectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    }

    private GetObjectRequest getObjectRequest(String key) throws IOException {
        return GetObjectRequest.builder()
                .bucket(awsBuckets.getMain())
                .key(key)
                .build();
    }
}

/*
---------------------- HÀM putObject ----------------------
    1/ PutObjectRequest.builder() → tạo một request để upload file lên S3.
    2/ .bucket(bucketName) → tên bucket cần upload.
    3/ .key(key) → đường dẫn / tên file trong bucket (ví dụ "images/avatar.png").
    4/ .build() → hoàn tất việc build request.
    5/ RequestBody.fromBytes(file) → tạo nội dung file từ mảng byte (byte[]).
    6/ s3Client.putObject(...) → gửi request lên S3 để upload file.

---------------------- HÀM getObject ----------------------
    1/ GetObjectRequest.builder() → tạo request để tải file từ S3.
    2/ .bucket(bucketName) → tên bucket chứa file.
    3/ .key(key) → đường dẫn hoặc tên file trên S3.
    4/ s3Client.getObject(objectRequest) → gửi request và nhận lại ResponseInputStream,
    là luồng dữ liệu file trả về.
    5/ response.readAllBytes() → đọc toàn bộ file thành byte[].
*/