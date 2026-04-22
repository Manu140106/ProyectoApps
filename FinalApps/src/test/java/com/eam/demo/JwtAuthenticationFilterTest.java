package com.eam.demo;

import com.eam.demo.entity.Role;
import com.eam.demo.security.CustomUserDetails;
import com.eam.demo.security.CustomUserDetailsService;
import com.eam.demo.security.JwtAuthenticationFilter;
import com.eam.demo.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter - Pruebas Unitarias")
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private CustomUserDetailsService customUserDetailsService;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private TestableJwtAuthenticationFilter filter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("FILTER - Sin header Authorization debe continuar")
    void doFilterInternal_NoAuthHeader_ShouldContinue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.execute(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, customUserDetailsService);
    }

    @Test
    @DisplayName("FILTER - Token invalido debe continuar sin autenticar")
    void doFilterInternal_InvalidToken_ShouldContinue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isTokenValid("invalid-token")).thenReturn(false);

        filter.execute(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).isTokenValid("invalid-token");
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    @DisplayName("FILTER - Token valido debe cargar usuario y setear autenticacion")
    void doFilterInternal_ValidToken_ShouldAuthenticate() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        CustomUserDetails userDetails = new CustomUserDetails(10L, 1L, "Admin Demo", "admin@demo.com", "hash", Role.ADMIN, true);
        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractUsername("valid-token")).thenReturn("admin@demo.com");
        when(customUserDetailsService.loadUserByUsername("admin@demo.com")).thenReturn(userDetails);

        filter.execute(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
        verify(filterChain).doFilter(request, response);
    }

    private static class TestableJwtAuthenticationFilter extends JwtAuthenticationFilter {
        TestableJwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService customUserDetailsService) {
            super(jwtService, customUserDetailsService);
        }

        void execute(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws Exception {
            super.doFilterInternal(request, response, filterChain);
        }
    }
}