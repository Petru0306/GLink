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
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir:uploads/products}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Nu s-a putut crea directorul pentru upload-uri.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        try {
            // Verifică dacă fișierul este gol
            if (file.isEmpty()) {
                throw new RuntimeException("Nu se poate salva un fișier gol");
            }

            // Verifică tipul fișierului
            String fileType = file.getContentType();
            if (fileType != null && !fileType.startsWith("image/")) {
                throw new RuntimeException("Doar imaginile sunt permise!");
            }
            // Generează un nume unic pentru fișier
            String originalFileName = StringUtils.cleanPath(
                    Objects.requireNonNull(file.getOriginalFilename(), "Numele fișierului nu poate fi null"));
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // Verifică dacă numele fișierului este valid
            if (newFileName.contains("..")) {
                throw new RuntimeException("Numele fișierului conține caractere invalide " + newFileName);
            }

            // Copiază fișierul în directorul de destinație
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return newFileName;

        } catch (IOException ex) {
            throw new RuntimeException("Nu s-a putut salva fișierul.", ex);
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Nu s-a putut șterge fișierul.", ex);
        }
    }

    public Path getFileLocation(String fileName) {
        return this.fileStorageLocation.resolve(fileName).normalize();
    }
}