package com.eam.demo;

import com.eam.demo.dto.AuthRequest;
import com.eam.demo.dto.AuthResponse;
import com.eam.demo.dto.OrganizationResponse;
import com.eam.demo.dto.RegisterOrganizationRequest;
import com.eam.demo.entity.Organization;
import com.eam.demo.entity.Role;
import com.eam.demo.entity.UserAccount;
import com.eam.demo.exception.BadRequestException;
import com.eam.demo.repository.OrganizationRepository;
import com.eam.demo.repository.UserAccountRepository;
import com.eam.demo.security.CustomUserDetails;
import com.eam.demo.security.JwtService;
import com.eam.demo.service.AuthService;
import com.eam.demo.service.TenantContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Pruebas Unitarias")
class AuthServiceTest {

    @Mock private OrganizationRepository organizationRepository;
    @Mock private UserAccountRepository userAccountRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private TenantContextService tenantContextService;

    @InjectMocks
    private AuthService authService;

    private Organization organization;
    private UserAccount adminUser;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        organization.setId(7L);
        organization.setName("Empresa Demo");
        organization.setCode("EMP01");
        organization.setActive(true);

        adminUser = new UserAccount();
        adminUser.setId(10L);
        adminUser.setFullName("Admin Demo");
        adminUser.setEmail("admin@demo.com");
        adminUser.setPassword("$2a$hash");
        adminUser.setRole(Role.ADMIN);
        adminUser.setActive(true);
        adminUser.setOrganization(organization);
    }

    @Test
    @DisplayName("REGISTER - Debe crear organizacion y usuario admin")
    void registerOrganization_ShouldCreateOrganizationAndAdmin() {
        RegisterOrganizationRequest request = new RegisterOrganizationRequest();
        request.setOrganizationName("  Empresa Demo  ");
        request.setOrganizationCode("  emp01  ");
        request.setAdminName("  Admin Demo  ");
        request.setAdminEmail("  Admin@Demo.com  ");
        request.setAdminPassword("password123");

        when(organizationRepository.existsByNameIgnoreCase("  Empresa Demo  ")).thenReturn(false);
        when(organizationRepository.existsByCodeIgnoreCase("  emp01  ")).thenReturn(false);
        when(userAccountRepository.existsByEmailIgnoreCase("  Admin@Demo.com  ")).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> {
            Organization saved = invocation.getArgument(0);
            saved.setId(7L);
            return saved;
        });
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

        OrganizationResponse response = authService.registerOrganization(request);

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getName()).isEqualTo("Empresa Demo");
        assertThat(response.getCode()).isEqualTo("EMP01");
        assertThat(response.isActive()).isTrue();

        verify(userAccountRepository).save(argThat(user ->
                user.getEmail().equals("admin@demo.com") &&
                        user.getRole() == Role.ADMIN &&
                        user.getOrganization().getId().equals(7L)));
    }

    @Test
    @DisplayName("REGISTER - Nombre duplicado debe lanzar BadRequestException")
    void registerOrganization_DuplicateName_ShouldThrow() {
        RegisterOrganizationRequest request = new RegisterOrganizationRequest();
        request.setOrganizationName("Empresa Demo");

        when(organizationRepository.existsByNameIgnoreCase("Empresa Demo")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerOrganization(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Ya existe una organizacion con ese nombre");
    }

    @Test
    @DisplayName("LOGIN - Credenciales validas deben retornar token")
    void login_ValidCredentials_ShouldReturnAuthResponse() {
        AuthRequest request = new AuthRequest();
        request.setEmail("admin@demo.com");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(org.springframework.security.core.Authentication.class));
        when(userAccountRepository.findByEmailIgnoreCase("admin@demo.com")).thenReturn(Optional.of(adminUser));
        when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUserId()).isEqualTo(10L);
        assertThat(response.getOrganizationId()).isEqualTo(7L);
        assertThat(response.getRole()).isEqualTo(Role.ADMIN);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(any(CustomUserDetails.class));
    }

    @Test
    @DisplayName("LOGIN - Usuario inactivo debe lanzar BadRequestException")
    void login_InactiveUser_ShouldThrow() {
        AuthRequest request = new AuthRequest();
        request.setEmail("admin@demo.com");
        request.setPassword("password123");

        UserAccount inactive = new UserAccount();
        inactive.setId(10L);
        inactive.setFullName("Admin Demo");
        inactive.setEmail("admin@demo.com");
        inactive.setPassword("$2a$hash");
        inactive.setRole(Role.ADMIN);
        inactive.setActive(false);
        inactive.setOrganization(organization);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(org.springframework.security.core.Authentication.class));
        when(userAccountRepository.findByEmailIgnoreCase("admin@demo.com")).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Usuario inactivo");
    }

    @Test
    @DisplayName("CURRENT ORGANIZATION - Debe retornar organizacion del tenant")
    void currentOrganization_ShouldMapCurrentTenant() {
        when(tenantContextService.currentOrganization()).thenReturn(organization);

        OrganizationResponse response = authService.currentOrganization();

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getCode()).isEqualTo("EMP01");
    }
}