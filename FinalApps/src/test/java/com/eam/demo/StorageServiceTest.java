package com.eam.demo;

import com.eam.demo.exception.BadRequestException;
import com.eam.demo.exception.NotFoundException;
import com.eam.demo.service.StorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

@DisplayName("StorageService - Pruebas Unitarias")
class StorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("STORE - Debe guardar archivo y sanitizar nombre")
    void store_ShouldSaveFile() {
        StorageService storageService = new StorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contrato final.pdf",
                "application/pdf",
                "contenido".getBytes()
        );

        String storedPath = storageService.store(file, "emp01");

        assertThat(storedPath).contains("EMP01");
        assertThat(storedPath).contains("contrato_final.pdf");
        assertThat(Files.exists(Path.of(storedPath))).isTrue();
    }

    @Test
    @DisplayName("STORE - Archivo vacio debe lanzar BadRequestException")
    void store_EmptyFile_ShouldThrow() {
        StorageService storageService = new StorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "", "text/plain", new byte[0]);

        assertThatThrownBy(() -> storageService.store(file, "emp01"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Debe adjuntar un archivo");
    }

    @Test
    @DisplayName("LOAD - Debe retornar recurso existente")
    void load_ShouldReturnResource() throws IOException {
        StorageService storageService = new StorageService(tempDir.toString());
        Path filePath = tempDir.resolve("test.txt");
        Files.writeString(filePath, "hola");

        Resource resource = storageService.load(filePath.toString());

        assertThat(resource.exists()).isTrue();
    }

    @Test
    @DisplayName("LOAD - Archivo inexistente debe lanzar NotFoundException")
    void load_MissingFile_ShouldThrow() {
        StorageService storageService = new StorageService(tempDir.toString());

        assertThatThrownBy(() -> storageService.load(tempDir.resolve("missing.txt").toString()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Archivo no encontrado");
    }
}