package com.eam.demo.service;

import com.eam.demo.exception.BadRequestException;
import com.eam.demo.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService {

    private final Path basePath;

    public StorageService(@Value("${storage.base-path}") String basePath) {
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.basePath);
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible inicializar el almacenamiento", e);
        }
    }

    public String store(MultipartFile file, String organizationCode) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Debe adjuntar un archivo");
        }

        String originalName = file.getOriginalFilename() == null ? "document" : file.getOriginalFilename();
        String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String fileName = UUID.randomUUID() + "_" + safeName;

        Path orgPath = basePath.resolve(organizationCode.toUpperCase());
        Path targetPath = orgPath.resolve(fileName);

        try {
            Files.createDirectories(orgPath);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath.toString();
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible guardar el archivo", e);
        }
    }

    public Resource load(String fullPath) {
        try {
            Path path = Paths.get(fullPath).normalize();
            if (!Files.exists(path)) {
                throw new NotFoundException("Archivo no encontrado");
            }

            return new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("No fue posible leer el archivo", e);
        }
    }
}
