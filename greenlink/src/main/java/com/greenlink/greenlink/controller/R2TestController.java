package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.service.R2StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/r2-test")
public class R2TestController {

    private static final Logger logger = LoggerFactory.getLogger(R2TestController.class);

    @Autowired
    private R2StorageService r2StorageService;

    @Value("${cloudflare.r2.enabled:false}")
    private boolean r2Enabled;

    @Value("${cloudflare.r2.bucket-name:product-images}")
    private String bucketName;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("r2Enabled", r2Enabled);
        status.put("bucketName", bucketName);
        status.put("serviceAvailable", r2StorageService != null);
        
        logger.info("R2 Status: enabled={}, bucket={}, serviceAvailable={}", 
                   r2Enabled, bucketName, r2StorageService != null);
        
        return ResponseEntity.ok(status);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> testUpload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("error", "File is empty");
                return ResponseEntity.badRequest().body(result);
            }

            String fileName = r2StorageService.storeFile(file);
            String fileUrl = r2StorageService.getFileUrl(fileName);
            
            result.put("success", true);
            result.put("fileName", fileName);
            result.put("fileUrl", fileUrl);
            result.put("fileSize", file.getSize());
            result.put("contentType", file.getContentType());
            
            logger.info("Test upload successful: fileName={}, fileUrl={}", fileName, fileUrl);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            logger.error("Test upload failed: {}", e.getMessage(), e);
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/check/{fileName}")
    public ResponseEntity<Map<String, Object>> checkFile(@PathVariable String fileName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean exists = r2StorageService.fileExists(fileName);
            String fileUrl = r2StorageService.getFileUrl(fileName);
            
            result.put("success", true);
            result.put("fileName", fileName);
            result.put("exists", exists);
            result.put("fileUrl", fileUrl);
            
            logger.info("File check: fileName={}, exists={}", fileName, exists);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            logger.error("File check failed: {}", e.getMessage(), e);
        }
        
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable String fileName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            r2StorageService.deleteFile(fileName);
            
            result.put("success", true);
            result.put("fileName", fileName);
            result.put("message", "File deleted successfully");
            
            logger.info("Test delete successful: fileName={}", fileName);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            logger.error("Test delete failed: {}", e.getMessage(), e);
        }
        
        return ResponseEntity.ok(result);
    }
} 