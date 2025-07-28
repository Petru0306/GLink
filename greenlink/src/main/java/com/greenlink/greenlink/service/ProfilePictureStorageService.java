package com.greenlink.greenlink.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ProfilePictureStorageService.class);
    private final Path fileStorageLocation;

    public ProfilePictureStorageService(@Value("${file.upload-dir:uploads}/profiles") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        logger.info("ProfilePictureStorageService initialized with storage location: {}", this.fileStorageLocation);

        try {
            Files.createDirectories(this.fileStorageLocation);
            logger.info("Successfully created/verified profile pictures directory: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            logger.error("Could not create the directory for profile pictures: {}", ex.getMessage(), ex);
            throw new RuntimeException("Could not create the directory for profile pictures.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        try {
            logger.info("Storing profile picture file: {}", file.getOriginalFilename());
            
            // Validate file is not empty
            if (file.isEmpty()) {
                logger.warn("Attempted to store empty file");
                throw new RuntimeException("Cannot store empty file");
            }

            // Validate file type
            String fileType = file.getContentType();
            if (fileType != null && !fileType.startsWith("image/")) {
                logger.warn("Invalid file type attempted: {}", fileType);
                throw new RuntimeException("Only image files are allowed!");
            }
            
            // Generate unique filename
            String originalFileName = StringUtils.cleanPath(
                    Objects.requireNonNull(file.getOriginalFilename(), "Filename cannot be null"));
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            logger.info("Generated filename: {} for original file: {}", newFileName, originalFileName);

            // Validate filename
            if (newFileName.contains("..")) {
                logger.warn("Filename contains invalid characters: {}", newFileName);
                throw new RuntimeException("Filename contains invalid characters " + newFileName);
            }

            // Store file
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            logger.info("Successfully stored profile picture at: {}", targetLocation);

            return newFileName;

        } catch (IOException ex) {
            logger.error("Could not store file: {}", ex.getMessage(), ex);
            throw new RuntimeException("Could not store file.", ex);
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            logger.info("Successfully deleted profile picture: {}", fileName);
        } catch (IOException ex) {
            logger.error("Could not delete file: {}", ex.getMessage(), ex);
            throw new RuntimeException("Could not delete file.", ex);
        }
    }

    public Path getFileLocation(String fileName) {
        return this.fileStorageLocation.resolve(fileName).normalize();
    }
} 