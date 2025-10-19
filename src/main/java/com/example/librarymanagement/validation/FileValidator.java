package com.example.librarymanagement.validation;

import com.example.librarymanagement.exception.FileValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Slf4j
@Component
public class FileValidator {
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/bmp"
    );

    public void validateImageFile(MultipartFile file) {
        validateFile(file, ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE, "image");
    }

    private void validateFile(MultipartFile file, Set<String> allowedTypes,
                              long maxSize, String fileTypeLabel) {
        if (file == null || file.isEmpty()) {
            throw new FileValidationException("File is required");
        }

        if (file.getSize() > maxSize) {
            throw new FileValidationException(String
                    .format("%s file size exceeds maximum allowed size of %d MB",
                            fileTypeLabel,
                            maxSize / (1024 * 1024)));
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new FileValidationException("Filename is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            throw new FileValidationException(
                    String.format("Invalid %s file type: %s. Allowed types: %s",
                            fileTypeLabel, contentType, allowedTypes)
            );
        }
        log.info("File validation passed: {} ({} bytes, {})",
                originalFilename, file.getSize(), contentType);
    }
}
