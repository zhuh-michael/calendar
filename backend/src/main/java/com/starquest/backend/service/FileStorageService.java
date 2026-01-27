package com.starquest.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.avatar-path:uploads/avatars/}")
    private String avatarPath;

    /**
     * Save an image file under avatarPath with date folder and uuid filename.
     * Returns relative url path like "uploads/avatars/2026-01-26/<uuid>.ext"
     */
    public String saveImage(MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("只支持图片文件");
            }

            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;

            String filePath = avatarPath + today + "/" + fileName;
            java.nio.file.Path path = java.nio.file.Paths.get(filePath);
            java.nio.file.Files.createDirectories(path.getParent());
            java.nio.file.Files.write(path, file.getBytes());

            // return relative url
            return "uploads/avatars/" +  today + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("图片保存失败: " + e.getMessage());
        }
    }
}


