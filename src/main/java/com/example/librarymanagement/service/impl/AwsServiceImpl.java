package com.example.librarymanagement.service.impl;

import com.example.librarymanagement.service.inter.AwsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AwsServiceImpl implements AwsService {
    private final S3Client s3Client;

    @Override
    public void putObject(String bucketName, String key, byte[] file) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.putObject(objectRequest, RequestBody.fromBytes(file));
    }

    // Trả về nội dung file dưới dạng byte[]
    @Override
    public byte[] getObject(String bucketName, String key) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        ResponseInputStream<GetObjectResponse> response = s3Client.getObject(objectRequest);

        try {
            return response.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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