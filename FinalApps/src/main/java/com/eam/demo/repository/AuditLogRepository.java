package com.eam.demo.repository;

import com.eam.demo.entity.AuditLog;
import com.eam.demo.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByDocumentOrderByCreatedAtDesc(Document document);
}
