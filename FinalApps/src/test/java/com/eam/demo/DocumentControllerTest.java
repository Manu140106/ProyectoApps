package com.eam.demo;

import com.eam.demo.controller.DocumentController;
import com.eam.demo.dto.AuditLogResponse;
import com.eam.demo.dto.DocumentResponse;
import com.eam.demo.dto.DocumentStatusUpdateRequest;
import com.eam.demo.entity.DocumentStatus;
import com.eam.demo.service.DocumentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentController - Pruebas Unitarias")
class DocumentControllerTest {

    @Mock private DocumentService documentService;

    @InjectMocks
    private DocumentController documentController;

    @Test
    @DisplayName("CREATE - Debe responder CREATED")
    void create_ShouldReturnCreated() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "hola".getBytes());
        DocumentResponse responseBody = new DocumentResponse(1L, "Contrato", "doc.pdf", "application/pdf", 4L, DocumentStatus.CREADO, 5L, "Contrato", 10L, "Admin", LocalDateTime.now(), LocalDateTime.now());

        when(documentService.create(5L, "Contrato", file)).thenReturn(responseBody);

        assertThat(documentController.create(5L, "Contrato", file).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(documentController.create(5L, "Contrato", file).getBody()).isEqualTo(responseBody);
    }

    @Test
    @DisplayName("LIST - Debe responder OK")
    void list_ShouldReturnOk() {
        List<DocumentResponse> responseBody = List.of(
                new DocumentResponse(1L, "Contrato", "doc.pdf", "application/pdf", 4L, DocumentStatus.CREADO, 5L, "Contrato", 10L, "Admin", LocalDateTime.now(), LocalDateTime.now())
        );
        when(documentService.list(null, null, null)).thenReturn(responseBody);

        assertThat(documentController.list(null, null, null).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(documentController.list(null, null, null).getBody()).isEqualTo(responseBody);
    }

    @Test
    @DisplayName("GET BY ID - Debe responder OK")
    void getById_ShouldReturnOk() {
        DocumentResponse responseBody = new DocumentResponse(1L, "Contrato", "doc.pdf", "application/pdf", 4L, DocumentStatus.CREADO, 5L, "Contrato", 10L, "Admin", LocalDateTime.now(), LocalDateTime.now());
        when(documentService.getById(1L)).thenReturn(responseBody);

        assertThat(documentController.getById(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(documentController.getById(1L).getBody()).isEqualTo(responseBody);
    }

    @Test
    @DisplayName("UPDATE STATUS - Debe responder OK")
    void updateStatus_ShouldReturnOk() {
        DocumentStatusUpdateRequest request = new DocumentStatusUpdateRequest();
        request.setStatus(DocumentStatus.EN_REVISION);
        DocumentResponse responseBody = new DocumentResponse(1L, "Contrato", "doc.pdf", "application/pdf", 4L, DocumentStatus.EN_REVISION, 5L, "Contrato", 10L, "Admin", LocalDateTime.now(), LocalDateTime.now());
        when(documentService.updateStatus(1L, request)).thenReturn(responseBody);

        assertThat(documentController.updateStatus(1L, request).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(documentController.updateStatus(1L, request).getBody()).isEqualTo(responseBody);
    }

    @Test
    @DisplayName("HISTORY - Debe responder OK")
    void history_ShouldReturnOk() {
        List<AuditLogResponse> responseBody = List.of(new AuditLogResponse(1L, "DOCUMENT_CREATED", "Creado", LocalDateTime.now(), 10L, "Admin"));
        when(documentService.history(1L)).thenReturn(responseBody);

        assertThat(documentController.history(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(documentController.history(1L).getBody()).isEqualTo(responseBody);
    }

    @Test
    @DisplayName("DOWNLOAD - Debe responder con cabecera de adjunto")
    void download_ShouldReturnAttachmentResponse() throws Exception {
        Path tempFile = Files.createTempFile("doc", ".pdf");
        Resource resource = new UrlResource(tempFile.toUri());
        DocumentResponse metadata = new DocumentResponse(1L, "Contrato", "doc final.pdf", "application/pdf", 4L, DocumentStatus.CREADO, 5L, "Contrato", 10L, "Admin", LocalDateTime.now(), LocalDateTime.now());

        when(documentService.getById(1L)).thenReturn(metadata);
        when(documentService.download(1L)).thenReturn(resource);

        var response = documentController.download(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains("attachment");
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
    }

    @Test
    @DisplayName("DELETE - Debe responder NO_CONTENT")
    void delete_ShouldReturnNoContent() {
        assertThat(documentController.delete(1L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}