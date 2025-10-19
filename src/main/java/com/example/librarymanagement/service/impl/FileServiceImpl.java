package com.example.librarymanagement.service.impl;

import com.example.librarymanagement.config.AwsBuckets;
import com.example.librarymanagement.dto.file.response.FileResponse;
import com.example.librarymanagement.entity.FileMetadata;
import com.example.librarymanagement.entity.User;
import com.example.librarymanagement.entity.UserProfile;
import com.example.librarymanagement.exception.UnauthorizedException;
import com.example.librarymanagement.repository.FileMetadataRepository;
import com.example.librarymanagement.repository.UserProfileRepository;
import com.example.librarymanagement.repository.UserRepository;
import com.example.librarymanagement.service.inter.FileService;
import com.example.librarymanagement.service.inter.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {
    private final S3Service s3Service;
    private final AwsBuckets awsBuckets;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final FileMetadataRepository fileMetadataRepository;

    @Override
    @Transactional
    public FileResponse uploadUserAvatar(MultipartFile file, Authentication authentication) {
        User user = validateUser(authentication);

        // Upload to S3
        String folderPath = awsBuckets.getFolders().getImagesSubFolders().getUsers();
        String s3Key = s3Service.uploadFile(file, folderPath);

        // Save metadata to DB
        FileMetadata metadata = createFileMetadata(user,
                s3Key,
                file,
                FileMetadata.FileType.IMAGE,
                FileMetadata.EntityType.USER_AVATAR,
                user.getId());

        fileMetadataRepository.save(metadata);

        // Update user profile avatar
        updateUserAvatar(user, s3Key);

        String presignUrl = s3Service.generatePresignedUrl(s3Key, Duration.ofHours(24));
        return mapToFileResponse(metadata, presignUrl);
    }

    private FileMetadata createFileMetadata(User user,
                                            String s3Key,
                                            MultipartFile file,
                                            FileMetadata.FileType fileType,
                                            FileMetadata.EntityType entityType,
                                            Integer entityId) {
        return FileMetadata.builder()
                .contentType(file.getContentType())
                .fileType(fileType)
                .originalFilename(file.getOriginalFilename())
                .fileSize(file.getSize())
                .s3Key(s3Key)
                .entityType(entityType)
                .entityId(entityId)
                .user(user)
                .build();
    }

    @Transactional
    private void updateUserAvatar(User user, String avatarS3Key) {
        UserProfile userProfile = user.getUserProfile();
        userProfile.setAvatarS3Key(avatarS3Key);
        userProfileRepository.save(userProfile);
    }

    private User validateUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private FileResponse mapToFileResponse(FileMetadata metadata, String presignUrl) {
        return FileResponse.builder()
                .key(metadata.getS3Key())
                .url(presignUrl)
                .fileName(metadata.getOriginalFilename())
                .contentType(metadata.getContentType())
                .size(metadata.getFileSize())
                .uploadedAt(LocalDateTime.now())
                .build();
    }
}
