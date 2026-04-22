package com.eam.demo;

import com.eam.demo.entity.Organization;
import com.eam.demo.entity.Role;
import com.eam.demo.entity.UserAccount;
import com.eam.demo.repository.UserAccountRepository;
import com.eam.demo.security.CustomUserDetails;
import com.eam.demo.security.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService - Pruebas Unitarias")
class CustomUserDetailsServiceTest {

    @Mock private UserAccountRepository userAccountRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("LOAD USER - Debe mapear el usuario a CustomUserDetails")
    void loadUserByUsername_ShouldMapUser() {
        Organization organization = new Organization();
        organization.setId(1L);

        UserAccount user = new UserAccount();
        user.setId(10L);
        user.setFullName("Admin Demo");
        user.setEmail("admin@demo.com");
        user.setPassword("hash");
        user.setRole(Role.ADMIN);
        user.setActive(true);
        user.setOrganization(organization);

        when(userAccountRepository.findByEmailIgnoreCase("admin@demo.com")).thenReturn(Optional.of(user));

        CustomUserDetails result = (CustomUserDetails) customUserDetailsService.loadUserByUsername("admin@demo.com");

        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getOrganizationId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("admin@demo.com");
        assertThat(result.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("LOAD USER - Usuario inexistente debe lanzar excepcion")
    void loadUserByUsername_MissingUser_ShouldThrow() {
        when(userAccountRepository.findByEmailIgnoreCase("missing@demo.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("missing@demo.com"))
                .isInstanceOf(org.springframework.security.core.userdetails.UsernameNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }
}