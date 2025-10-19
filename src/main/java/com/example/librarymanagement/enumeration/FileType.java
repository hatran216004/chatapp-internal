package com.example.librarymanagement.enumeration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.MediaType;

import java.util.Arrays;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
// access = AccessLevel.PACKAGE: quy định access modifier của constructor Lombok tạo ra
public enum FileType {
    JPG("jpg", MediaType.IMAGE_JPEG),
    JPEG("jpeg", MediaType.IMAGE_JPEG),
    TXT("txt", MediaType.TEXT_PLAIN),
    PNG("png", MediaType.IMAGE_PNG),
    PDF("pdf", MediaType.APPLICATION_PDF);

    private final String extension;
    private final MediaType mediaType;

    // Method to get MediaType based on the filename's extension
    public static MediaType fromFilename(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        String fileExtension = (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);

        return Arrays.stream(values())
                .filter(e -> e.getExtension().equals(fileExtension))
                .findFirst()
                .map(FileType::getMediaType)
                // Default to octet-stream if no matching media type found (một file bình thường, không biết loại gì)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
    }
}
