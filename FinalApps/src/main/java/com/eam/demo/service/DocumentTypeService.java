package com.eam.demo.service;

import com.eam.demo.dto.DocumentTypeRequest;
import com.eam.demo.dto.DocumentTypeResponse;
import com.eam.demo.entity.DocumentType;
import com.eam.demo.entity.Organization;
import com.eam.demo.exception.BadRequestException;
import com.eam.demo.exception.NotFoundException;
import com.eam.demo.repository.DocumentTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DocumentTypeService {

    private final DocumentTypeRepository documentTypeRepository;
    private final TenantContextService tenantContextService;

    public DocumentTypeService(DocumentTypeRepository documentTypeRepository, TenantContextService tenantContextService) {
        this.documentTypeRepository = documentTypeRepository;
        this.tenantContextService = tenantContextService;
    }

    public List<DocumentTypeResponse> list() {
        Organization organization = tenantContextService.currentOrganization();
        return documentTypeRepository.findAllByOrganizationOrderByNameAsc(organization)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DocumentTypeResponse create(DocumentTypeRequest request) {
        Organization organization = tenantContextService.currentOrganization();

        if (documentTypeRepository.existsByNameIgnoreCaseAndOrganization(request.getName(), organization)) {
            throw new BadRequestException("Ya existe ese tipo documental");
        }

        DocumentType type = new DocumentType();
        type.setName(request.getName().trim());
        type.setDescription(request.getDescription());
        type.setActive(request.getActive() == null || request.getActive());
        type.setOrganization(organization);

        return toResponse(documentTypeRepository.save(type));
    }

    @Transactional
    public DocumentTypeResponse update(Long id, DocumentTypeRequest request) {
        Organization organization = tenantContextService.currentOrganization();

        DocumentType type = documentTypeRepository.findByIdAndOrganization(id, organization)
                .orElseThrow(() -> new NotFoundException("Tipo documental no encontrado"));

        type.setName(request.getName().trim());
        type.setDescription(request.getDescription());
        if (request.getActive() != null) {
            type.setActive(request.getActive());
        }

        return toResponse(documentTypeRepository.save(type));
    }

    @Transactional
    public void delete(Long id) {
        Organization organization = tenantContextService.currentOrganization();
        DocumentType type = documentTypeRepository.findByIdAndOrganization(id, organization)
                .orElseThrow(() -> new NotFoundException("Tipo documental no encontrado"));

        documentTypeRepository.delete(type);
    }

    public DocumentType getEntityOrThrow(Long id, Organization organization) {
        return documentTypeRepository.findByIdAndOrganization(id, organization)
                .orElseThrow(() -> new NotFoundException("Tipo documental no encontrado"));
    }

    private DocumentTypeResponse toResponse(DocumentType type) {
        return new DocumentTypeResponse(
                type.getId(),
                type.getName(),
                type.getDescription(),
                type.isActive(),
                type.getOrganization().getId()
        );
    }
}
