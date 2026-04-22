package com.eam.demo;

import com.eam.demo.entity.Role;
import com.eam.demo.exception.ForbiddenOperationException;
import com.eam.demo.security.CustomUserDetails;
import com.eam.demo.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SecurityUtils - Pruebas Unitarias")
class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET CURRENT USER - Debe retornar el principal autenticado")
    void getCurrentUser_ShouldReturnAuthenticatedPrincipal() {
        CustomUserDetails user = new CustomUserDetails(10L, 1L, "Admin Demo", "admin@demo.com", "hash", Role.ADMIN, true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        );

        assertThat(SecurityUtils.getCurrentUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("GET CURRENT USER - Sin autenticacion debe lanzar ForbiddenOperationException")
    void getCurrentUser_NoAuthentication_ShouldThrow() {
        assertThatThrownBy(SecurityUtils::getCurrentUser)
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("Usuario no autenticado");
    }

    @Test
    @DisplayName("GET CURRENT USER - Principal invalido debe lanzar ForbiddenOperationException")
    void getCurrentUser_InvalidPrincipal_ShouldThrow() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymous", null)
        );

        assertThatThrownBy(SecurityUtils::getCurrentUser)
                .isInstanceOf(ForbiddenOperationException.class);
    }
}