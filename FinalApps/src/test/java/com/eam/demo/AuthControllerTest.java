package com.eam.demo;

import com.eam.demo.controller.AuthController;
import com.eam.demo.dto.AuthRequest;
import com.eam.demo.dto.AuthResponse;
import com.eam.demo.dto.OrganizationResponse;
import com.eam.demo.dto.RegisterOrganizationRequest;
import com.eam.demo.entity.Role;
import com.eam.demo.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController - Pruebas Unitarias")
class AuthControllerTest {

    @Mock private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("REGISTER - Debe responder CREATED")
    void registerOrganization_ShouldReturnCreated() {
        RegisterOrganizationRequest request = new RegisterOrganizationRequest();
        OrganizationResponse responseBody = new OrganizationResponse(1L, "Empresa", "EMP01", true);

        when(authService.registerOrganization(request)).thenReturn(responseBody);

        assertThat(authController.registerOrganization(request).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(authController.registerOrganization(request).getBody()).isEqualTo(responseBody);
    }

    @Test
    @DisplayName("LOGIN - Debe responder OK")
    void login_ShouldReturnOk() {
        AuthRequest request = new AuthRequest();
        AuthResponse responseBody = new AuthResponse("jwt", "Bearer", 10L, 1L, "Admin", "admin@demo.com", Role.ADMIN);

        when(authService.login(request)).thenReturn(responseBody);

        assertThat(authController.login(request).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(authController.login(request).getBody()).isEqualTo(responseBody);
    }

    @Test
    @DisplayName("CURRENT ORGANIZATION - Debe responder OK")
    void currentOrganization_ShouldReturnOk() {
        OrganizationResponse responseBody = new OrganizationResponse(1L, "Empresa", "EMP01", true);
        when(authService.currentOrganization()).thenReturn(responseBody);

        assertThat(authController.currentOrganization().getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(authController.currentOrganization().getBody()).isEqualTo(responseBody);
    }
}