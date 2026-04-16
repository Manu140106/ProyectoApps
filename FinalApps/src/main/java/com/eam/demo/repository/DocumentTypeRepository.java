package com.eam.demo.repository;

import com.eam.demo.entity.DocumentType;
import com.eam.demo.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, Long> {
    List<DocumentType> findAllByOrganizationOrderByNameAsc(Organization organization);
    Optional<DocumentType> findByIdAndOrganization(Long id, Organization organization);
    boolean existsByNameIgnoreCaseAndOrganization(String name, Organization organization);
}
