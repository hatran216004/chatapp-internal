package com.example.librarymanagement.service.inter;

import com.example.librarymanagement.dto.file.response.FileResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    // User iamges
    FileResponse uploadUserAvatar(MultipartFile file, Authentication authentication);
}
