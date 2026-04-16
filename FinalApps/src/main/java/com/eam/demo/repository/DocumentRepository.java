package com.eam.demo.repository;

import com.eam.demo.entity.Document;
import com.eam.demo.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {
    List<Document> findAllByOrganizationOrderByCreatedAtDesc(Organization organization);
    Optional<Document> findByIdAndOrganization(Long id, Organization organization);
}
