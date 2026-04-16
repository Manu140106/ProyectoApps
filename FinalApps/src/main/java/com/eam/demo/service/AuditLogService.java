package com.eam.demo.service;

import com.eam.demo.dto.AuditLogResponse;
import com.eam.demo.entity.AuditLog;
import com.eam.demo.entity.Document;
import com.eam.demo.entity.UserAccount;
import com.eam.demo.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void register(Document document, UserAccount actor, String action, String details) {
        AuditLog log = new AuditLog();
        log.setDocument(document);
        log.setActor(actor);
        log.setAction(action);
        log.setDetails(details);

        auditLogRepository.save(log);
    }

    public List<AuditLogResponse> byDocument(Document document) {
        return auditLogRepository.findAllByDocumentOrderByCreatedAtDesc(document)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getAction(),
                log.getDetails(),
                log.getCreatedAt(),
                log.getActor().getId(),
                log.getActor().getFullName()
        );
    }
}
