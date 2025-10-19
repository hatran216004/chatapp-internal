package com.example.librarymanagement.service.inter;

public interface AwsService {
    void putObject(String bucketName, String key, byte[] file);

    byte[] getObject(String bucketName, String key);
}
