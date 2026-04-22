package com.eam.demo;

import com.eam.demo.dto.DocumentTypeRequest;
import com.eam.demo.dto.DocumentTypeResponse;
import com.eam.demo.entity.DocumentType;
import com.eam.demo.entity.Organization;
import com.eam.demo.exception.BadRequestException;
import com.eam.demo.exception.NotFoundException;
import com.eam.demo.repository.DocumentTypeRepository;
import com.eam.demo.service.DocumentTypeService;
import com.eam.demo.service.TenantContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentTypeService - Pruebas Unitarias")
class DocumentTypeServiceTest {

    @Mock private DocumentTypeRepository documentTypeRepository;
    @Mock private TenantContextService tenantContextService;

    @InjectMocks
    private DocumentTypeService documentTypeService;

    private Organization organization;
    private DocumentType documentType;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        organization.setId(1L);
        organization.setName("Empresa Demo");
        organization.setCode("EMP01");

        documentType = new DocumentType();
        documentType.setId(5L);
        documentType.setName("Contrato");
        documentType.setDescription("Contratos de servicio");
        documentType.setActive(true);
        documentType.setOrganization(organization);
    }

    @Test
    @DisplayName("LIST - Debe retornar tipos documentales mapeados")
    void list_ShouldReturnMappedDocumentTypes() {
        when(tenantContextService.currentOrganization()).thenReturn(organization);
        when(documentTypeRepository.findAllByOrganizationOrderByNameAsc(organization)).thenReturn(List.of(documentType));

        List<DocumentTypeResponse> result = documentTypeService.list();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Contrato");
    }

    @Test
    @DisplayName("CREATE - Debe crear tipo documental con active por defecto")
    void create_ShouldCreateDocumentType() {
        DocumentTypeRequest request = new DocumentTypeRequest();
        request.setName("  Contrato  ");
        request.setDescription("  Contratos de servicio  ");
        request.setActive(null);

        when(tenantContextService.currentOrganization()).thenReturn(organization);
        when(documentTypeRepository.existsByNameIgnoreCaseAndOrganization("  Contrato  ", organization)).thenReturn(false);
        when(documentTypeRepository.save(any(DocumentType.class))).thenAnswer(invocation -> {
            DocumentType saved = invocation.getArgument(0);
            saved.setId(9L);
            return saved;
        });

        DocumentTypeResponse response = documentTypeService.create(request);

        assertThat(response.getId()).isEqualTo(9L);
        assertThat(response.getName()).isEqualTo("Contrato");
        assertThat(response.isActive()).isTrue();
    }

    @Test
    @DisplayName("CREATE - Nombre duplicado debe lanzar BadRequestException")
    void create_DuplicateName_ShouldThrow() {
        DocumentTypeRequest request = new DocumentTypeRequest();
        request.setName("Contrato");

        when(tenantContextService.currentOrganization()).thenReturn(organization);
        when(documentTypeRepository.existsByNameIgnoreCaseAndOrganization("Contrato", organization)).thenReturn(true);

        assertThatThrownBy(() -> documentTypeService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Ya existe ese tipo documental");
    }

    @Test
    @DisplayName("UPDATE - Debe mantener active cuando no se envia")
    void update_ShouldKeepActiveWhenNotProvided() {
        DocumentTypeRequest request = new DocumentTypeRequest();
        request.setName("Contrato actualizado");
        request.setDescription("Actualizado");
        request.setActive(null);

        when(tenantContextService.currentOrganization()).thenReturn(organization);
        when(documentTypeRepository.findByIdAndOrganization(5L, organization)).thenReturn(Optional.of(documentType));
        when(documentTypeRepository.save(any(DocumentType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentTypeResponse response = documentTypeService.update(5L, request);

        assertThat(response.getName()).isEqualTo("Contrato actualizado");
        assertThat(response.isActive()).isTrue();
    }

    @Test
    @DisplayName("DELETE - Tipo documental existente debe eliminarse")
    void delete_ShouldRemoveDocumentType() {
        when(tenantContextService.currentOrganization()).thenReturn(organization);
        when(documentTypeRepository.findByIdAndOrganization(5L, organization)).thenReturn(Optional.of(documentType));

        assertThatCode(() -> documentTypeService.delete(5L)).doesNotThrowAnyException();
        verify(documentTypeRepository).delete(documentType);
    }

    @Test
    @DisplayName("GET ENTITY - Debe retornar entidad o lanzar NotFoundException")
    void getEntityOrThrow_ShouldReturnEntity() {
        when(documentTypeRepository.findByIdAndOrganization(5L, organization)).thenReturn(Optional.of(documentType));

        assertThat(documentTypeService.getEntityOrThrow(5L, organization)).isEqualTo(documentType);

        when(documentTypeRepository.findByIdAndOrganization(6L, organization)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> documentTypeService.getEntityOrThrow(6L, organization))
                .isInstanceOf(NotFoundException.class);
    }
}