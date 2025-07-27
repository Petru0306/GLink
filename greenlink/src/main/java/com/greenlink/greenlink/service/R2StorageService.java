package com.greenlink.greenlink.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.util.UUID;
import java.util.Objects;

@Service
public class R2StorageService {

    private static final Logger logger = LoggerFactory.getLogger(R2StorageService.class);

    @Autowired(required = false)
    private S3Client s3Client;

    @Value("${cloudflare.r2.bucket-name:product-images}")
    private String bucketName;

    @Value("${cloudflare.r2.account-id}")
    private String accountId;

    @Value("${cloudflare.r2.public-url:}")
    private String publicUrl;

    @Value("${cloudflare.r2.enabled:false}")
    private boolean r2Enabled;

    @Autowired
    private FileStorageService fileStorageService;

    public String storeFile(MultipartFile file) {
        if (!r2Enabled || s3Client == null) {
            logger.info("R2 is disabled, falling back to local storage");
            return fileStorageService.storeFile(file);
        }

        try {
            // Validate file
            if (file.isEmpty()) {
                throw new RuntimeException("Cannot store empty file");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Only image files are allowed");
            }

            // Generate unique filename
            String originalFileName = Objects.requireNonNull(file.getOriginalFilename(), "Filename cannot be null");
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // Upload to R2
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(newFileName)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            logger.info("File uploaded to R2 successfully: {}", newFileName);
            return newFileName;

        } catch (IOException e) {
            logger.error("Error uploading file to R2: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to R2", e);
        }
    }

    public void deleteFile(String fileName) {
        if (!r2Enabled || s3Client == null) {
            logger.info("R2 is disabled, using local storage delete");
            fileStorageService.deleteFile(fileName);
            return;
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            logger.info("File deleted from R2 successfully: {}", fileName);

        } catch (Exception e) {
            logger.error("Error deleting file from R2: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete file from R2", e);
        }
    }

    public boolean fileExists(String fileName) {
        if (!r2Enabled || s3Client == null) {
            logger.info("R2 is disabled, checking local storage");
            return fileStorageService.getFileLocation(fileName).toFile().exists();
        }

        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            logger.error("Error checking file existence in R2: {}", e.getMessage(), e);
            return false;
        }
    }

    public String getFileUrl(String fileName) {
        if (!r2Enabled || s3Client == null) {
            // Return local file URL
            return "/files/products/" + fileName;
        }

        // Return R2 public URL - using the configured public URL
        if (publicUrl != null && !publicUrl.isEmpty()) {
            return publicUrl + "/" + fileName;
        } else {
            // Fallback to account ID format
            return "https://pub-" + accountId + ".r2.dev/" + fileName;
        }
    }

    public String extractFileNameFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        
        // Handle R2 URLs
        if (imageUrl.contains("r2.dev")) {
            return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        }
        
        // Handle local URLs
        if (imageUrl.contains("/files/products/")) {
            return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        }
        
        // If it's already just a filename
        return imageUrl;
    }
} 