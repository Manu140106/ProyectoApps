package com.eam.demo.repository;

import com.eam.demo.entity.Organization;
import com.eam.demo.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<UserAccount> findAllByOrganizationOrderByCreatedAtDesc(Organization organization);
    Optional<UserAccount> findByIdAndOrganization(Long id, Organization organization);
}
