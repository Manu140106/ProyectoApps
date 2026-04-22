package com.eam.demo;

import com.eam.demo.entity.Organization;
import com.eam.demo.entity.Role;
import com.eam.demo.entity.UserAccount;
import com.eam.demo.exception.NotFoundException;
import com.eam.demo.repository.OrganizationRepository;
import com.eam.demo.repository.UserAccountRepository;
import com.eam.demo.security.CustomUserDetails;
import com.eam.demo.service.TenantContextService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantContextService - Pruebas Unitarias")
class TenantContextServiceTest {

    @Mock private UserAccountRepository userAccountRepository;
    @Mock private OrganizationRepository organizationRepository;

    @InjectMocks
    private TenantContextService tenantContextService;

    private CustomUserDetails currentUser;
    private UserAccount userEntity;
    private Organization organization;

    @BeforeEach
    void setUp() {
        currentUser = new CustomUserDetails(10L, 1L, "Admin Demo", "admin@demo.com", "hash", Role.ADMIN, true);

        userEntity = new UserAccount();
        userEntity.setId(10L);
        userEntity.setFullName("Admin Demo");
        userEntity.setEmail("admin@demo.com");
        userEntity.setRole(Role.ADMIN);
        userEntity.setActive(true);

        organization = new Organization();
        organization.setId(1L);
        organization.setName("Empresa Demo");
        organization.setCode("EMP01");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser, null, currentUser.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("CURRENT PRINCIPAL - Debe retornar el usuario autenticado")
    void currentPrincipal_ShouldReturnAuthenticatedUser() {
        assertThat(tenantContextService.currentPrincipal()).isEqualTo(currentUser);
    }

    @Test
    @DisplayName("CURRENT USER - Debe retornar la entidad del usuario autenticado")
    void currentUserEntity_ShouldReturnUserEntity() {
        when(userAccountRepository.findById(10L)).thenReturn(Optional.of(userEntity));

        assertThat(tenantContextService.currentUserEntity()).isEqualTo(userEntity);
    }

    @Test
    @DisplayName("CURRENT USER - Usuario inexistente debe lanzar NotFoundException")
    void currentUserEntity_MissingUser_ShouldThrow() {
        when(userAccountRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tenantContextService.currentUserEntity())
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Usuario autenticado no encontrado");
    }

    @Test
    @DisplayName("CURRENT ORGANIZATION - Debe retornar la organizacion del usuario")
    void currentOrganization_ShouldReturnOrganization() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));

        assertThat(tenantContextService.currentOrganization()).isEqualTo(organization);
    }

    @Test
    @DisplayName("CURRENT ORGANIZATION - Organizacion inexistente debe lanzar NotFoundException")
    void currentOrganization_MissingOrganization_ShouldThrow() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tenantContextService.currentOrganization())
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Organizacion no encontrada");
    }
}