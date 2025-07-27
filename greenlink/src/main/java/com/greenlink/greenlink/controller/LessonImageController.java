package com.greenlink.greenlink.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.greenlink.greenlink.model.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/lesson-images")
public class LessonImageController {

    private static final Logger logger = LoggerFactory.getLogger(LessonImageController.class);
    private final String uploadDir = "uploads/lesson-images/";

    @PostMapping("/upload")
    public ResponseEntity<?> uploadLessonImage(
            @RequestParam("image") MultipartFile file,
            @RequestParam("lessonId") Long lessonId,
            @AuthenticationPrincipal User currentUser) {
        
        logger.info("Image upload request received for lesson {} by user {}", lessonId, currentUser != null ? currentUser.getId() : "null");
        
        Map<String, Object> response = new HashMap<>();
        
        if (currentUser == null) {
            logger.warn("User not authenticated for image upload");
            response.put("success", false);
            response.put("message", "User not authenticated");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (file.isEmpty()) {
            logger.warn("Empty file provided for image upload");
            response.put("success", false);
            response.put("message", "No image file provided");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            logger.info("Processing image upload: original filename = {}", file.getOriginalFilename());
            
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                logger.info("Creating upload directory: {}", uploadPath.toAbsolutePath());
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                response.put("success", false);
                response.put("message", "Invalid filename");
                return ResponseEntity.badRequest().body(response);
            }
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = "lesson_" + lessonId + "_user_" + currentUser.getId() + "_" + UUID.randomUUID().toString() + fileExtension;
            
            logger.info("Generated unique filename: {}", uniqueFilename);
            
            // Save file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath);
            
            logger.info("File saved successfully at: {}", filePath.toAbsolutePath());
            
            // Return the URL path
            String imageUrl = "/uploads/lesson-images/" + uniqueFilename;
            
            logger.info("Returning image URL: {}", imageUrl);
            
            response.put("success", true);
            response.put("imageUrl", imageUrl);
            response.put("message", "Image uploaded successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 