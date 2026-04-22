package com.eam.demo;

import com.eam.demo.dto.UserRequest;
import com.eam.demo.dto.UserResponse;
import com.eam.demo.entity.Organization;
import com.eam.demo.entity.Role;
import com.eam.demo.entity.UserAccount;
import com.eam.demo.exception.BadRequestException;
import com.eam.demo.exception.NotFoundException;
import com.eam.demo.repository.UserAccountRepository;
import com.eam.demo.service.TenantContextService;
import com.eam.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Pruebas Unitarias")
class UserServiceTest {

    @Mock private UserAccountRepository userAccountRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TenantContextService tenantContextService;

    @InjectMocks
    private UserService userService;

    private Organization organization;
    private UserAccount existingUser;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        organization.setId(1L);
        organization.setName("Empresa Demo");
        organization.setCode("EMP01");

        existingUser = new UserAccount();
        existingUser.setId(11L);
        existingUser.setFullName("Laura Torres");
        existingUser.setEmail("laura@demo.com");
        existingUser.setPassword("$2a$hash");
        existingUser.setRole(Role.USER);
        existingUser.setActive(true);
        existingUser.setOrganization(organization);
    }

    @Test
    @DisplayName("LIST - Debe retornar usuarios mapeados de la organizacion")
    void listByCurrentOrganization_ShouldReturnMappedUsers() {
        when(tenantContextService.currentOrganization()).thenReturn(organization);
        when(userAccountRepository.findAllByOrganizationOrderByCreatedAtDesc(organization)).thenReturn(List.of(existingUser));

        List<UserResponse> result = userService.listByCurrentOrganization();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("laura@demo.com");
        assertThat(result.get(0).getOrganizationId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("CREATE - Debe usar rol USER por defecto y cifrar clave")
    void create_ShouldUseDefaultRoleAndEncodePassword() {
        UserRequest request = new UserRequest();
        request.setFullName("  Laura Torres  ");
        request.setEmail("  laura@demo.com  ");
        request.setPassword("password123");
        request.setRole(null);
        request.setActive(null);

        when(tenantContextService.currentOrganization()).thenReturn(organization);
        when(userAccountRepository.existsByEmailIgnoreCase("  laura@demo.com  ")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> {
            UserAccount saved = invocation.getArgument(0);
            saved.setId(22L);
            return saved;
        });

        UserResponse response = userService.create(request);

        assertThat(response.getId()).isEqualTo(22L);
        assertThat(response.getFullName()).isEqualTo("Laura Torres");
        assertThat(response.getEmail()).isEqualTo("laura@demo.com");
        assertThat(response.getRole()).isEqualTo(Role.USER);
        assertThat(response.isActive()).isTrue();
    }

    @Test
    @DisplayName("CREATE - Email duplicado debe lanzar BadRequestException")
    void create_DuplicateEmail_ShouldThrow() {
        UserRequest request = new UserRequest();
        request.setFullName("Laura Torres");
        request.setEmail("laura@demo.com");
        request.setPassword("password123");
        request.setRole(Role.USER);

        when(tenantContextService.currentOrganization()).thenReturn(organization);
        when(userAccountRepository.existsByEmailIgnoreCase("laura@demo.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Ya existe un usuario con ese email");
    }

    @Test
    @DisplayName("UPDATE - Debe actualizar datos y password cuando se envia")
    void update_ShouldUpdateUserAndPassword() {
        UserRequest request = new UserRequest();
        request.setFullName("Laura Actualizada");
        request.setEmail("laura@demo.com");
        request.setPassword("newPassword123");
        request.setRole(Role.ADMIN);
        request.setActive(false);

        when(tenantContextService.currentOrganization()).thenReturn(organization);
        when(userAccountRepository.findByIdAndOrganization(11L, organization)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encoded-new-password");
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.update(11L, request);

        assertThat(response.getFullName()).isEqualTo("Laura Actualizada");
        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
        assertThat(response.isActive()).isFalse();
        assertThat(response.getEmail()).isEqualTo("laura@demo.com");
    }

    @Test
    @DisplayName("UPDATE - Usuario inexistente debe lanzar NotFoundException")
    void update_NonExistentUser_ShouldThrow() {
        UserRequest request = new UserRequest();
        request.setFullName("Laura Actualizada");
        request.setEmail("laura@demo.com");
        request.setRole(Role.USER);

        when(tenantContextService.currentOrganization()).thenReturn(organization);
        when(userAccountRepository.findByIdAndOrganization(11L, organization)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(11L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }
}