package com.example.librarymanagement.repository;

import com.example.librarymanagement.entity.FileMetadata;
import com.example.librarymanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Integer> {
    Optional<FileMetadata> findByS3Key(String s3Key);

    List<FileMetadata> findByUserAndDeletedAtIsNotNull(User user);

    List<FileMetadata> findByEntityTypeAndEntityIdAndDeletedAtIsNotNull(
            FileMetadata.EntityType entityType,
            Integer entityId);
}
