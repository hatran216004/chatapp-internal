package com.example.librarymanagement.controller;

import com.example.librarymanagement.dto.ApiResponse;
import com.example.librarymanagement.dto.file.response.FileResponse;
import com.example.librarymanagement.service.inter.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileUploadController {
    private final FileService fileService;

    @PostMapping("/upload/user-avatar")
    public ResponseEntity<ApiResponse<FileResponse>> uploadUserAvatar(@RequestParam("file") MultipartFile file,
                                                                      Authentication authentication) {
        FileResponse res = fileService.uploadUserAvatar(file, authentication);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", res));
    }
}
