package com.eam.demo;

import com.eam.demo.dto.AuditLogResponse;
import com.eam.demo.dto.DocumentResponse;
import com.eam.demo.dto.DocumentStatusUpdateRequest;
import com.eam.demo.entity.*;
import com.eam.demo.exception.NotFoundException;
import com.eam.demo.repository.DocumentRepository;
import com.eam.demo.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentService - Pruebas Unitarias")
class DocumentServiceTest {

    @Mock private DocumentRepository documentRepository;
    @Mock private TenantContextService tenantContextService;
    @Mock private DocumentTypeService documentTypeService;
    @Mock private StorageService storageService;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private DocumentService documentService;

    private Organization org;
    private UserAccount owner;
    private DocumentType docType;
    private Document savedDocument;

    @BeforeEach
    void setUp() {
        org = new Organization();
        org.setId(1L);
        org.setCode("ORG01");

        owner = new UserAccount();
        owner.setId(10L);
        owner.setFullName("Ana López");

        docType = new DocumentType();
        docType.setId(5L);
        docType.setName("Contrato");
        docType.setOrganization(org);

        savedDocument = new Document();
        savedDocument.setId(1L);
        savedDocument.setTitle("Contrato Marco");
        savedDocument.setOriginalFileName("contrato.pdf");
        savedDocument.setStoragePath("/storage/ORG01/contrato.pdf");
        savedDocument.setContentType("application/pdf");
        savedDocument.setSize(2048L);
        savedDocument.setStatus(DocumentStatus.CREADO);
        savedDocument.setDocumentType(docType);
        savedDocument.setOwner(owner);
        savedDocument.setOrganization(org);
    }

    @Test
    @DisplayName("CREATE - Documento válido debe retornar DocumentResponse con ID")
    void create_ValidFile_ShouldReturnDocumentResponse() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("contrato.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getSize()).thenReturn(2048L);
        when(file.isEmpty()).thenReturn(false);

        when(tenantContextService.currentOrganization()).thenReturn(org);
        when(tenantContextService.currentUserEntity()).thenReturn(owner);
        when(documentTypeService.getEntityOrThrow(5L, org)).thenReturn(docType);
        when(storageService.store(file, "ORG01")).thenReturn("/storage/ORG01/contrato.pdf");
        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);

        DocumentResponse result = documentService.create(5L, "Contrato Marco", file);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Contrato Marco");
        assertThat(result.getStatus()).isEqualTo(DocumentStatus.CREADO);

        verify(documentRepository, times(1)).save(any(Document.class));
        verify(auditLogService, times(1)).register(any(), eq(owner), eq("DOCUMENT_CREATED"), anyString());
    }

    @Test
    @DisplayName("CREATE - contentType null debe usar application/octet-stream")
    void create_NullContentType_ShouldUseOctetStream() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("file.bin");
        when(file.getContentType()).thenReturn(null);
        when(file.getSize()).thenReturn(512L);

        Document docWithOctet = new Document();
        docWithOctet.setId(2L);
        docWithOctet.setTitle("Archivo");
        docWithOctet.setOriginalFileName("file.bin");
        docWithOctet.setContentType("application/octet-stream");
        docWithOctet.setSize(512L);
        docWithOctet.setStatus(DocumentStatus.CREADO);
        docWithOctet.setDocumentType(docType);
        docWithOctet.setOwner(owner);
        docWithOctet.setOrganization(org);

        when(tenantContextService.currentOrganization()).thenReturn(org);
        when(tenantContextService.currentUserEntity()).thenReturn(owner);
        when(documentTypeService.getEntityOrThrow(5L, org)).thenReturn(docType);
        when(storageService.store(file, "ORG01")).thenReturn("/storage/ORG01/file.bin");
        when(documentRepository.save(any(Document.class))).thenReturn(docWithOctet);

        DocumentResponse result = documentService.create(5L, "Archivo", file);

        assertThat(result.getContentType()).isEqualTo("application/octet-stream");
    }

    @Test
    @DisplayName("LIST - Sin filtros debe retornar todos los documentos de la organización")
    @SuppressWarnings("unchecked")
    void list_NoFilters_ShouldReturnAllDocuments() {
        when(tenantContextService.currentOrganization()).thenReturn(org);
        when(documentRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(savedDocument));

        List<DocumentResponse> result = documentService.list(null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Contrato Marco");
        verify(documentRepository, times(1)).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    @DisplayName("LIST - Con filtro de estado debe retornar documentos filtrados")
    @SuppressWarnings("unchecked")
    void list_WithStatusFilter_ShouldFilterByStatus() {
        when(tenantContextService.currentOrganization()).thenReturn(org);
        when(documentRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(savedDocument));

        List<DocumentResponse> result = documentService.list(DocumentStatus.CREADO, null, null);

        assertThat(result).hasSize(1);
        verify(documentRepository, times(1)).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    @DisplayName("LIST - Con rango de fechas debe aplicar los filtros de fecha")
    @SuppressWarnings("unchecked")
    void list_WithDateRange_ShouldApplyDateFilters() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59);

        when(tenantContextService.currentOrganization()).thenReturn(org);
        when(documentRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(savedDocument));

        List<DocumentResponse> result = documentService.list(null, start, end);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("LIST - Sin documentos debe retornar lista vacía")
    @SuppressWarnings("unchecked")
    void list_NoDocuments_ShouldReturnEmptyList() {
        when(tenantContextService.currentOrganization()).thenReturn(org);
        when(documentRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of());

        List<DocumentResponse> result = documentService.list(null, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("GET BY ID - Documento existente debe retornar DocumentResponse")
    void getById_ExistingDocument_ShouldReturnResponse() {
        when(tenantContextService.currentOrganization()).thenReturn(org);
        when(documentRepository.findByIdAndOrganization(1L, org)).thenReturn(Optional.of(savedDocument));

        DocumentResponse result = documentService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Contrato Marco");
    }

    @Test
    @DisplayName("GET BY ID - Documento inexistente debe lanzar NotFoundException")
    void getById_NonExistentDocument_ShouldThrowNotFoundException() {
        when(tenantContextService.currentOrganization()).thenReturn(org);
        when(documentRepository.findByIdAndOrganization(999L, org)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Documento no encontrado");
    }

    @Test
    @DisplayName("UPDATE STATUS - Estado válido debe actualizar y registrar auditoría")
    void updateStatus_ValidRequest_ShouldUpdateAndAudit() {
        DocumentStatusUpdateRequest req = new DocumentStatusUpdateRequest();
        req.setStatus(DocumentStatus.EN_REVISION);
        req.setDetails("Enviado a revisión");

        Document updated = new Document();
        updated.setId(1L);
        updated.setTitle("Contrato Marco");
        updated.setOriginalFileName("contrato.pdf");
        updated.setContentType("application/pdf");
        updated.setSize(2048L);
        updated.setStatus(DocumentStatus.EN_REVISION);
        updated.setDocumentType(docType);
        updated.setOwner(owner);
        updated.setOrganization(org);

        when(tenantContextService.currentOrganization()).thenReturn(org);
        when(tenantContextService.currentUserEntity()).thenReturn(owner);
        when(documentRepository.findByIdAndOrganization(1L, org)).thenReturn(Optional.of(savedDocument));
        when(documentRepository.save(any(Document.class))).thenReturn(updated);

        DocumentResponse result = documentService.updateStatus(1L, req);

        assertThat(result.getStatus()).isEqualTo(DocumentStatus.EN_REVISION);
        verify(auditLogService, times(1)).register(any(), eq(owner), eq("DOCUMENT_STATUS_CHANGED"), eq("Enviado a revisión"));
    }

    @Test
    @DisplayName("UPDATE STATUS - Details null debe usar mensaje genérico en auditoría")
    void updateStatus_NullDetails_ShouldUseDefaultAuditMessage() {
        DocumentStatusUpdateRequest req = new DocumentStatusUpdateRequest();
        req.setStatus(DocumentStatus.APROBADO);
        req.setDetails(null);

        Document updated = new Document();
        updated.setId(1L);
        updated.setTitle("Contrato Marco");
        updated.setOriginalFileName("contrato.pdf");
        updated.setContentType("application/pdf");
        updated.setSize(2048L);
        updated.setStatus(DocumentStatus.APROBADO);
        updated.setDocumentType(docType);
        updated.setOwner(owner);
        updated.setOrganization(org);

        when(tenantContextService.currentOrganization()).thenReturn(org);
        when(tenantContextService.currentUserEntity()).thenReturn(owner);
        when(documentRepository.findByIdAndOrganization(1L, org)).thenReturn(Optional.of(savedDocument));
        when(documentRepository.save(any(Document.class))).thenReturn(updated);

        documentService.updateStatus(1L, req);

        verify(auditLogService, times(1)).register(any(), any(), eq("DOCUMENT_STATUS_CHANGED"), eq("Cambio de estado"));
    }

    @Test
    @DisplayName("DELETE - Documento existente debe eliminar y registrar auditoría")
    void delete_ExistingDocument_ShouldDeleteAndAudit() {
        when(tenantContextService.currentOrganization()).thenReturn(org);
        when(tenantContextService.currentUserEntity()).thenReturn(owner);
        when(documentRepository.findByIdAndOrganization(1L, org)).thenReturn(Optional.of(savedDocument));

        assertThatCode(() -> documentService.delete(1L)).doesNotThrowAnyException();

        verify(auditLogService, times(1)).register(eq(savedDocument), eq(owner), eq("DOCUMENT_DELETED"), anyString());
        verify(documentRepository, times(1)).delete(savedDocument);
    }

    @Test
    @DisplayName("DELETE - Documento inexistente debe lanzar NotFoundException")
    void delete_NonExistentDocument_ShouldThrowNotFoundException() {
        when(tenantContextService.currentOrganization()).thenReturn(org);
        when(documentRepository.findByIdAndOrganization(999L, org)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.delete(999L))
                .isInstanceOf(NotFoundException.class);

        verify(documentRepository, never()).delete(any(Document.class));
    }

    @Test
    @DisplayName("DOWNLOAD - Documento existente debe retornar Resource del archivo")
    void download_ExistingDocument_ShouldReturnResource() {
        Resource mockResource = mock(UrlResource.class);

        when(tenantContextService.currentOrganization()).thenReturn(org);
        when(documentRepository.findByIdAndOrganization(1L, org)).thenReturn(Optional.of(savedDocument));
        when(storageService.load("/storage/ORG01/contrato.pdf")).thenReturn(mockResource);

        Resource result = documentService.download(1L);

        assertThat(result).isNotNull();
        verify(storageService, times(1)).load("/storage/ORG01/contrato.pdf");
    }

    @Test
    @DisplayName("HISTORY - Debe retornar lista de eventos de auditoría del documento")
    void history_ExistingDocument_ShouldReturnAuditLogs() {
        AuditLogResponse logEntry = new AuditLogResponse(1L, "DOCUMENT_CREATED", "Documento creado",
                LocalDateTime.now(), owner.getId(), owner.getFullName());

        when(tenantContextService.currentOrganization()).thenReturn(org);
        when(documentRepository.findByIdAndOrganization(1L, org)).thenReturn(Optional.of(savedDocument));
        when(auditLogService.byDocument(savedDocument)).thenReturn(List.of(logEntry));

        List<AuditLogResponse> result = documentService.history(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAction()).isEqualTo("DOCUMENT_CREATED");
    }

    @Test
    @DisplayName("normalizeStartDate - Fecha válida debe normalizarse a inicio del día")
    void normalizeStartDate_ValidDate_ShouldReturnStartOfDay() {
        LocalDateTime input = LocalDateTime.of(2026, 3, 15, 14, 30);
        LocalDateTime result = documentService.normalizeStartDate(input);

        assertThat(result.getHour()).isEqualTo(0);
        assertThat(result.getMinute()).isEqualTo(0);
        assertThat(result.getSecond()).isEqualTo(0);
        assertThat(result.toLocalDate()).isEqualTo(input.toLocalDate());
    }

    @Test
    @DisplayName("normalizeStartDate - Null debe retornar null")
    void normalizeStartDate_Null_ShouldReturnNull() {
        assertThat(documentService.normalizeStartDate(null)).isNull();
    }

    @Test
    @DisplayName("normalizeEndDate - Fecha válida debe normalizarse a fin del día")
    void normalizeEndDate_ValidDate_ShouldReturnEndOfDay() {
        LocalDateTime input = LocalDateTime.of(2026, 3, 15, 9, 0);
        LocalDateTime result = documentService.normalizeEndDate(input);

        assertThat(result.getHour()).isEqualTo(23);
        assertThat(result.getMinute()).isEqualTo(59);
        assertThat(result.toLocalDate()).isEqualTo(input.toLocalDate());
    }

    @Test
    @DisplayName("normalizeEndDate - Null debe retornar null")
    void normalizeEndDate_Null_ShouldReturnNull() {
        assertThat(documentService.normalizeEndDate(null)).isNull();
    }
}