package com.eam.demo;

import com.eam.demo.dto.AuditLogResponse;
import com.eam.demo.entity.AuditLog;
import com.eam.demo.entity.Document;
import com.eam.demo.entity.DocumentStatus;
import com.eam.demo.entity.DocumentType;
import com.eam.demo.entity.Organization;
import com.eam.demo.entity.UserAccount;
import com.eam.demo.repository.AuditLogRepository;
import com.eam.demo.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogService - Pruebas Unitarias")
class AuditLogServiceTest {

    @Mock private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private Document document;
    private UserAccount actor;
    private AuditLog savedLog;

    @BeforeEach
    void setUp() {
        Organization org = new Organization();
        org.setId(1L);

        DocumentType docType = new DocumentType();
        docType.setId(5L);
        docType.setName("Contrato");
        docType.setOrganization(org);

        actor = new UserAccount();
        actor.setId(10L);
        actor.setFullName("Ana López");
        actor.setOrganization(org);

        document = new Document();
        document.setId(1L);
        document.setTitle("Contrato Marco");
        document.setStatus(DocumentStatus.CREADO);
        document.setDocumentType(docType);
        document.setOwner(actor);
        document.setOrganization(org);

        savedLog = new AuditLog();
        savedLog.setId(100L);
        savedLog.setDocument(document);
        savedLog.setActor(actor);
        savedLog.setAction("DOCUMENT_CREATED");
        savedLog.setDetails("Documento creado");
        savedLog.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("REGISTER - Debe guardar AuditLog con los datos correctos")
    void register_ShouldSaveAuditLogWithCorrectData() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(savedLog);

        auditLogService.register(document, actor, "DOCUMENT_CREATED", "Documento creado");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog captured = captor.getValue();
        assertThat(captured.getDocument()).isEqualTo(document);
        assertThat(captured.getActor()).isEqualTo(actor);
        assertThat(captured.getAction()).isEqualTo("DOCUMENT_CREATED");
        assertThat(captured.getDetails()).isEqualTo("Documento creado");
    }

    @Test
    @DisplayName("REGISTER - Acción de cambio de estado debe guardarse correctamente")
    void register_StatusChange_ShouldSaveWithStatusChangeAction() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(savedLog);

        auditLogService.register(document, actor, "DOCUMENT_STATUS_CHANGED", "Estado cambiado a EN_REVISION");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        assertThat(captor.getValue().getAction()).isEqualTo("DOCUMENT_STATUS_CHANGED");
        assertThat(captor.getValue().getDetails()).isEqualTo("Estado cambiado a EN_REVISION");
    }

    @Test
    @DisplayName("REGISTER - Acción de eliminación debe guardarse correctamente")
    void register_DocumentDeleted_ShouldSaveWithDeleteAction() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(savedLog);

        auditLogService.register(document, actor, "DOCUMENT_DELETED", "Documento eliminado");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        assertThat(captor.getValue().getAction()).isEqualTo("DOCUMENT_DELETED");
    }

    @Test
    @DisplayName("REGISTER - details null debe guardarse como null")
    void register_NullDetails_ShouldSaveWithNullDetails() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(savedLog);

        auditLogService.register(document, actor, "DOCUMENT_CREATED", null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        assertThat(captor.getValue().getDetails()).isNull();
    }

    @Test
    @DisplayName("BY DOCUMENT - Debe retornar lista de AuditLogResponse mapeada correctamente")
    void byDocument_ShouldReturnMappedAuditLogResponses() {
        AuditLog log2 = new AuditLog();
        log2.setId(101L);
        log2.setDocument(document);
        log2.setActor(actor);
        log2.setAction("DOCUMENT_STATUS_CHANGED");
        log2.setDetails("Enviado a revisión");
        log2.setCreatedAt(LocalDateTime.now());

        when(auditLogRepository.findAllByDocumentOrderByCreatedAtDesc(document))
                .thenReturn(Arrays.asList(savedLog, log2));

        List<AuditLogResponse> result = auditLogService.byDocument(document);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(100L);
        assertThat(result.get(0).getAction()).isEqualTo("DOCUMENT_CREATED");
        assertThat(result.get(0).getActorUserId()).isEqualTo(10L);
        assertThat(result.get(0).getActorName()).isEqualTo("Ana López");
        assertThat(result.get(1).getAction()).isEqualTo("DOCUMENT_STATUS_CHANGED");

        verify(auditLogRepository, times(1)).findAllByDocumentOrderByCreatedAtDesc(document);
    }

    @Test
    @DisplayName("BY DOCUMENT - Sin logs debe retornar lista vacía")
    void byDocument_NoLogs_ShouldReturnEmptyList() {
        when(auditLogRepository.findAllByDocumentOrderByCreatedAtDesc(document)).thenReturn(List.of());

        List<AuditLogResponse> result = auditLogService.byDocument(document);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("BY DOCUMENT - El mapeo debe incluir todos los campos requeridos")
    void byDocument_ShouldMapAllFieldsCorrectly() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 3, 15, 10, 30);
        savedLog.setCreatedAt(timestamp);

        when(auditLogRepository.findAllByDocumentOrderByCreatedAtDesc(document))
                .thenReturn(List.of(savedLog));

        List<AuditLogResponse> result = auditLogService.byDocument(document);

        AuditLogResponse response = result.get(0);
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getAction()).isEqualTo("DOCUMENT_CREATED");
        assertThat(response.getDetails()).isEqualTo("Documento creado");
        assertThat(response.getCreatedAt()).isEqualTo(timestamp);
        assertThat(response.getActorUserId()).isEqualTo(10L);
        assertThat(response.getActorName()).isEqualTo("Ana López");
    }
}