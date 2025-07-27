package com.greenlink.greenlink.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.Objects;

@Service
public class ProfilePictureStorageService {

    private final Path fileStorageLocation;

    public ProfilePictureStorageService(@Value("${file.upload-dir:uploads}/profiles") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory for profile pictures.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        try {
            // Validate file is not empty
            if (file.isEmpty()) {
                throw new RuntimeException("Cannot store empty file");
            }

            // Validate file type
            String fileType = file.getContentType();
            if (fileType != null && !fileType.startsWith("image/")) {
                throw new RuntimeException("Only image files are allowed!");
            }
            
            // Generate unique filename
            String originalFileName = StringUtils.cleanPath(
                    Objects.requireNonNull(file.getOriginalFilename(), "Filename cannot be null"));
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // Validate filename
            if (newFileName.contains("..")) {
                throw new RuntimeException("Filename contains invalid characters " + newFileName);
            }

            // Store file
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return newFileName;

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file.", ex);
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file.", ex);
        }
    }

    public Path getFileLocation(String fileName) {
        return this.fileStorageLocation.resolve(fileName).normalize();
    }
} 