package com.eam.demo.controller;

import com.eam.demo.dto.DocumentTypeRequest;
import com.eam.demo.dto.DocumentTypeResponse;
import com.eam.demo.service.DocumentTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/document-types")
@Tag(name = "Tipos Documentales", description = "Catalogo de tipos de documento por organizacion")
@SecurityRequirement(name = "bearerAuth")
public class DocumentTypeController {

    private final DocumentTypeService documentTypeService;

    public DocumentTypeController(DocumentTypeService documentTypeService) {
        this.documentTypeService = documentTypeService;
    }

    @GetMapping
    @Operation(summary = "Listar tipos documentales", description = "Lista los tipos documentales activos de la organizacion actual")
    public ResponseEntity<List<DocumentTypeResponse>> list() {
        return ResponseEntity.ok(documentTypeService.list());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear tipo documental", description = "Crea un tipo documental para la organizacion actual")
    public ResponseEntity<DocumentTypeResponse> create(@Valid @RequestBody DocumentTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentTypeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar tipo documental", description = "Actualiza un tipo documental de la organizacion actual")
    public ResponseEntity<DocumentTypeResponse> update(@PathVariable Long id, @Valid @RequestBody DocumentTypeRequest request) {
        return ResponseEntity.ok(documentTypeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar tipo documental", description = "Elimina un tipo documental de la organizacion actual")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
