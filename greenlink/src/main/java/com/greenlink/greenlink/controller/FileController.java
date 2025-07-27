package com.greenlink.greenlink.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/files")
public class FileController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private final Path fileStorageLocation;

    public FileController(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
        logger.info("FileController initialized with storage location: {}", this.fileStorageLocation);
    }

    @GetMapping("/products/{fileName:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveProductImage(@PathVariable String fileName) {
        try {
            logger.info("Request to serve product image: {}", fileName);
            Path filePath = this.fileStorageLocation.resolve("products").resolve(fileName);
            logger.info("Looking for file at: {}", filePath.toAbsolutePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                logger.info("File exists, serving: {}", fileName);
                String contentType = determineContentType(fileName);
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                
                logger.warn("File not found: {}, serving placeholder", fileName);
                Resource placeholderResource = new ClassPathResource("static/images/placeholder-product.jpg");
                if (placeholderResource.exists()) {
                    logger.info("Serving placeholder image instead");
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType("image/jpeg"))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"placeholder-product.jpg\"")
                            .body(placeholderResource);
                } else {
                    logger.error("Placeholder image not found either!");
                    return ResponseEntity.notFound().build();
                }
            }
        } catch (MalformedURLException e) {
            logger.error("Error serving file {}: {}", fileName, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/images/products/{fileName:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveStaticProductImage(@PathVariable String fileName) {
        logger.info("Request to serve static product image: {}", fileName);
        Resource resource = new ClassPathResource("static/images/products/" + fileName);
        
        if (resource.exists()) {
            logger.info("Static image exists, serving: {}", fileName);
            String contentType = determineContentType(fileName);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            logger.warn("Static image not found: {}", fileName);
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/profiles/{fileName:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveProfileImage(@PathVariable String fileName) {
        try {
            logger.info("Request to serve profile image: {}", fileName);
            Path filePath = this.fileStorageLocation.resolve("profiles").resolve(fileName);
            logger.info("Looking for profile file at: {}", filePath.toAbsolutePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                logger.info("Profile file exists, serving: {}", fileName);
                String contentType = determineContentType(fileName);
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                logger.warn("Profile file not found: {}, serving default avatar", fileName);
                Resource defaultAvatarResource = new ClassPathResource("static/images/logo.svg");
                if (defaultAvatarResource.exists()) {
                    logger.info("Serving default avatar instead");
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType("image/svg+xml"))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"logo.svg\"")
                            .body(defaultAvatarResource);
                } else {
                    logger.error("Default avatar not found either!");
                    return ResponseEntity.notFound().build();
                }
            }
        } catch (MalformedURLException e) {
            logger.error("Error serving profile file {}: {}", fileName, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    private String determineContentType(String fileName) {
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return switch (fileExtension) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
} 