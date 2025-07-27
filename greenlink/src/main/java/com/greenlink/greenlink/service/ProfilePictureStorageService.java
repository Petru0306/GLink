package com.greenlink.greenlink.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired(required = false)
    private R2StorageService r2StorageService;

    @Value("${cloudflare.r2.enabled:false}")
    private boolean r2Enabled;

    @Value("${cloudflare.r2.public-url:}")
    private String r2PublicUrl;

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
        // Use R2 storage if enabled and available
        if (r2Enabled && r2StorageService != null) {
            logger.info("Using R2 storage for profile picture upload");
            try {
                String fileName = r2StorageService.storeFile(file);
                // Return the full R2 URL for profile pictures
                return r2StorageService.getFileUrl(fileName);
            } catch (Exception e) {
                logger.error("Failed to upload to R2, falling back to local storage: {}", e.getMessage());
                // Fall back to local storage if R2 fails
            }
        }

        // Fall back to local storage
        logger.info("Using local storage for profile picture upload");
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

            return "/uploads/profiles/" + newFileName;

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file.", ex);
        }
    }

    public void deleteFile(String fileName) {
        // Use R2 storage if enabled and available
        if (r2Enabled && r2StorageService != null) {
            logger.info("Using R2 storage for profile picture deletion");
            try {
                // Extract filename from URL if it's a full URL
                String extractedFileName = extractFileNameFromUrl(fileName);
                r2StorageService.deleteFile(extractedFileName);
                return;
            } catch (Exception e) {
                logger.error("Failed to delete from R2, falling back to local storage: {}", e.getMessage());
                // Fall back to local storage if R2 fails
            }
        }

        // Fall back to local storage
        logger.info("Using local storage for profile picture deletion");
        try {
            // Extract filename from path if it's a full path
            String extractedFileName = extractFileNameFromUrl(fileName);
            Path filePath = this.fileStorageLocation.resolve(extractedFileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file.", ex);
        }
    }

    public Path getFileLocation(String fileName) {
        return this.fileStorageLocation.resolve(fileName).normalize();
    }

    private String extractFileNameFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        
        // Handle R2 URLs
        if (imageUrl.contains("r2.dev")) {
            return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        }
        
        // Handle local URLs
        if (imageUrl.contains("/uploads/profiles/")) {
            return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        }
        
        // If it's already just a filename
        return imageUrl;
    }
} 