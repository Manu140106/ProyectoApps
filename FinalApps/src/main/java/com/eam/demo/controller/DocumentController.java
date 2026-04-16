package com.eam.demo.controller;

import com.eam.demo.dto.AuditLogResponse;
import com.eam.demo.dto.DocumentResponse;
import com.eam.demo.dto.DocumentStatusUpdateRequest;
import com.eam.demo.entity.DocumentStatus;
import com.eam.demo.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Documentos", description = "Gestion documental, estados, historial y descarga")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Crear documento", description = "Carga un archivo y crea un documento en la organizacion actual")
    public ResponseEntity<DocumentResponse> create(
            @Parameter(description = "Id del tipo documental", required = true)
            @RequestParam Long documentTypeId,
            @Parameter(description = "Titulo del documento", required = true)
            @RequestParam String title,
            @Parameter(description = "Archivo a subir", required = true)
            @RequestParam MultipartFile file
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.create(documentTypeId, title, file));
    }

    @GetMapping
        @Operation(summary = "Listar documentos", description = "Lista documentos de la organizacion con filtros opcionales")
    public ResponseEntity<List<DocumentResponse>> list(
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(documentService.list(status, startDate, endDate));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener documento por id", description = "Retorna metadatos de un documento de la organizacion actual")
    public ResponseEntity<DocumentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getById(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Cambiar estado de documento", description = "Actualiza el estado y registra evento de auditoria")
    public ResponseEntity<DocumentResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody DocumentStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(documentService.updateStatus(id, request));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Consultar historial", description = "Retorna trazabilidad/auditoria del documento")
    public ResponseEntity<List<AuditLogResponse>> history(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.history(id));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Descargar archivo", description = "Descarga el archivo fisico asociado al documento")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        DocumentResponse metadata = documentService.getById(id);
        Resource file = documentService.download(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getOriginalFileName() + "\"")
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .body(file);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar documento", description = "Elimina el documento y su archivo asociado")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
