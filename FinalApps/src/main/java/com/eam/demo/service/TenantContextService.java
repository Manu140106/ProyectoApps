package com.eam.demo.service;

import com.eam.demo.entity.Organization;
import com.eam.demo.entity.UserAccount;
import com.eam.demo.exception.NotFoundException;
import com.eam.demo.repository.OrganizationRepository;
import com.eam.demo.repository.UserAccountRepository;
import com.eam.demo.security.CustomUserDetails;
import com.eam.demo.security.SecurityUtils;
import org.springframework.stereotype.Service;

@Service
public class TenantContextService {

    private final UserAccountRepository userAccountRepository;
    private final OrganizationRepository organizationRepository;

    public TenantContextService(UserAccountRepository userAccountRepository, OrganizationRepository organizationRepository) {
        this.userAccountRepository = userAccountRepository;
        this.organizationRepository = organizationRepository;
    }

    public CustomUserDetails currentPrincipal() {
        return SecurityUtils.getCurrentUser();
    }

    public UserAccount currentUserEntity() {
        CustomUserDetails user = currentPrincipal();
        return userAccountRepository.findById(user.getUserId())
                .orElseThrow(() -> new NotFoundException("Usuario autenticado no encontrado"));
    }

    public Organization currentOrganization() {
        CustomUserDetails user = currentPrincipal();
        return organizationRepository.findById(user.getOrganizationId())
                .orElseThrow(() -> new NotFoundException("Organizacion no encontrada"));
    }
}
