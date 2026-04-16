package com.eam.demo.service;

import com.eam.demo.dto.AuditLogResponse;
import com.eam.demo.dto.DocumentResponse;
import com.eam.demo.dto.DocumentStatusUpdateRequest;
import com.eam.demo.entity.Document;
import com.eam.demo.entity.DocumentStatus;
import com.eam.demo.entity.DocumentType;
import com.eam.demo.entity.Organization;
import com.eam.demo.entity.UserAccount;
import com.eam.demo.exception.NotFoundException;
import com.eam.demo.repository.DocumentRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final TenantContextService tenantContextService;
    private final DocumentTypeService documentTypeService;
    private final StorageService storageService;
    private final AuditLogService auditLogService;

    public DocumentService(
            DocumentRepository documentRepository,
            TenantContextService tenantContextService,
            DocumentTypeService documentTypeService,
            StorageService storageService,
            AuditLogService auditLogService
    ) {
        this.documentRepository = documentRepository;
        this.tenantContextService = tenantContextService;
        this.documentTypeService = documentTypeService;
        this.storageService = storageService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public DocumentResponse create(Long documentTypeId, String title, MultipartFile file) {
        Organization organization = tenantContextService.currentOrganization();
        UserAccount currentUser = tenantContextService.currentUserEntity();
        DocumentType type = documentTypeService.getEntityOrThrow(documentTypeId, organization);

        String storagePath = storageService.store(file, organization.getCode());

        Document document = new Document();
        document.setTitle(title.trim());
        document.setOriginalFileName(file.getOriginalFilename());
        document.setStoragePath(storagePath);
        document.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
        document.setSize(file.getSize());
        document.setStatus(DocumentStatus.CREADO);
        document.setDocumentType(type);
        document.setOwner(currentUser);
        document.setOrganization(organization);

        Document saved = documentRepository.save(document);
        auditLogService.register(saved, currentUser, "DOCUMENT_CREATED", "Documento creado");

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> list(DocumentStatus status, LocalDateTime startDate, LocalDateTime endDate) {
        Organization organization = tenantContextService.currentOrganization();

        Specification<Document> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("organization"), organization));

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), normalizeStartDate(startDate)));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), normalizeEndDate(endDate)));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };

        return documentRepository
                .findAll(specification, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DocumentResponse updateStatus(Long id, DocumentStatusUpdateRequest request) {
        Document document = getOwnedDocument(id);
        UserAccount currentUser = tenantContextService.currentUserEntity();

        document.setStatus(request.getStatus());
        Document saved = documentRepository.save(document);

        String details = request.getDetails() == null ? "Cambio de estado" : request.getDetails();
        auditLogService.register(saved, currentUser, "DOCUMENT_STATUS_CHANGED", details);

        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Document document = getOwnedDocument(id);
        UserAccount currentUser = tenantContextService.currentUserEntity();

        auditLogService.register(document, currentUser, "DOCUMENT_DELETED", "Documento eliminado");
        documentRepository.delete(document);
    }

    @Transactional(readOnly = true)
    public Resource download(Long id) {
        Document document = getOwnedDocument(id);
        return storageService.load(document.getStoragePath());
    }

    @Transactional(readOnly = true)
    public DocumentResponse getById(Long id) {
        return toResponse(getOwnedDocument(id));
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> history(Long id) {
        Document document = getOwnedDocument(id);
        return auditLogService.byDocument(document);
    }

    private Document getOwnedDocument(Long id) {
        Organization organization = tenantContextService.currentOrganization();
        return documentRepository.findByIdAndOrganization(id, organization)
                .orElseThrow(() -> new NotFoundException("Documento no encontrado"));
    }

    private DocumentResponse toResponse(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getOriginalFileName(),
                document.getContentType(),
                document.getSize(),
                document.getStatus(),
                document.getDocumentType().getId(),
                document.getDocumentType().getName(),
                document.getOwner().getId(),
                document.getOwner().getFullName(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    public LocalDateTime normalizeStartDate(LocalDateTime startDate) {
        return startDate == null ? null : LocalDateTime.of(startDate.toLocalDate(), LocalTime.MIN);
    }

    public LocalDateTime normalizeEndDate(LocalDateTime endDate) {
        return endDate == null ? null : LocalDateTime.of(endDate.toLocalDate(), LocalTime.MAX);
    }
}
