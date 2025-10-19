package com.example.librarymanagement.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cloud.aws.buckets")
@Getter
@Setter
public class AwsBuckets {
    private String main; // Bucket name
    private Folders folders = new Folders(); // Nhóm tất cả prefix thư mục

    @Getter
    @Setter
    public static class Folders {
        private String images = "images/";

        // Các thư mục con trong images
        private ImagesSubFolders imagesSubFolders = new ImagesSubFolders();
    }

    @Getter
    @Setter
    public static class ImagesSubFolders {
        private String users = "images/users/";
    }

    // ---------------- Helper ----------------

    public String getFullPath(String folder, String subFolder, String filename) {
        return folder + subFolder + filename;
    }

    public String getUserImagePath(String filename) {
        return folders.getImagesSubFolders().getUsers() + filename; // images/users/ + filename
    }
}
