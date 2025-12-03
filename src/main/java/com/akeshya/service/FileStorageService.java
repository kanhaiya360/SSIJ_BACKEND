// FileStorageService.java
package com.akeshya.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Store a file in the upload directory
     */
    public String storeFile(MultipartFile file) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Created upload directory: {}", uploadPath.toAbsolutePath());
        }

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + fileExtension;

        // Validate file type
        if (!isValidImageFile(file)) {
            throw new IllegalArgumentException("Invalid file type. Only images are allowed.");
        }

        // Validate file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size too large. Maximum size is 5MB.");
        }

        // Copy file to target location
        Path targetLocation = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        log.info("File stored successfully: {}", filename);
        
        // Return the relative file path that can be used in URLs
        return "/uploads/" + filename;
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg"; // default extension
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    /**
     * Validate if file is an image
     */
    private boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        return contentType.startsWith("image/") && 
               (contentType.equals("image/jpeg") || 
                contentType.equals("image/png") || 
                contentType.equals("image/jpg") || 
                contentType.equals("image/gif") ||
                contentType.equals("image/webp"));
    }

    /**
     * Delete a file from the upload directory
     */
    public boolean deleteFile(String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return false;
            }
            
            // Extract filename from path
            String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
            Path fileToDelete = Paths.get(uploadDir).resolve(filename);
            
            boolean deleted = Files.deleteIfExists(fileToDelete);
            if (deleted) {
                log.info("File deleted successfully: {}", filename);
            } else {
                log.warn("File not found for deletion: {}", filename);
            }
            return deleted;
            
        } catch (IOException e) {
            log.error("Error deleting file {}: {}", filePath, e.getMessage());
            return false;
        }
    }

    /**
     * Check if a file exists
     */
    public boolean fileExists(String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return false;
            }
            
            String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
            Path filePathToCheck = Paths.get(uploadDir).resolve(filename);
            
            return Files.exists(filePathToCheck);
        } catch (Exception e) {
            log.error("Error checking file existence {}: {}", filePath, e.getMessage());
            return false;
        }
    }

    /**
     * Get the absolute path of a stored file
     */
    public Path getFilePath(String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return null;
            }
            
            String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
            return Paths.get(uploadDir).resolve(filename);
        } catch (Exception e) {
            log.error("Error getting file path {}: {}", filePath, e.getMessage());
            return null;
        }
    }
}