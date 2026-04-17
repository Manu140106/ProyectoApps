package com.eam.demo;

import com.eam.demo.entity.Role;
import com.eam.demo.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CustomUserDetails - Pruebas Unitarias")
class CustomUserDetailsTest {

    private CustomUserDetails adminDetails;
    private CustomUserDetails userDetails;
    private CustomUserDetails inactiveDetails;

    @BeforeEach
    void setUp() {
        adminDetails = new CustomUserDetails(
                1L, 10L, "Admin Demo", "admin@demo.com", "$2a$hash_admin", Role.ADMIN, true
        );
        userDetails = new CustomUserDetails(
                2L, 10L, "Laura Torres", "laura@demo.com", "$2a$hash_user", Role.USER, true
        );
        inactiveDetails = new CustomUserDetails(
                3L, 10L, "Inactivo", "inactive@demo.com", "$2a$hash", Role.USER, false
        );
    }

    @Test
    @DisplayName("getUsername debe retornar el email del usuario")
    void getUsername_ShouldReturnEmail() {
        assertThat(adminDetails.getUsername()).isEqualTo("admin@demo.com");
        assertThat(userDetails.getUsername()).isEqualTo("laura@demo.com");
    }

    @Test
    @DisplayName("getPassword debe retornar el hash de la contraseña")
    void getPassword_ShouldReturnHashedPassword() {
        assertThat(adminDetails.getPassword()).isEqualTo("$2a$hash_admin");
    }

    @Test
    @DisplayName("getAuthorities - ADMIN debe tener autoridad ROLE_ADMIN")
    void getAuthorities_Admin_ShouldHaveRoleAdmin() {
        Collection<? extends GrantedAuthority> authorities = adminDetails.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("getAuthorities - USER debe tener autoridad ROLE_USER")
    void getAuthorities_User_ShouldHaveRoleUser() {
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("Usuario activo debe tener todas las propiedades de cuenta en true")
    void activeUser_ShouldHaveAllAccountPropertiesTrue() {
        assertThat(adminDetails.isAccountNonExpired()).isTrue();
        assertThat(adminDetails.isAccountNonLocked()).isTrue();
        assertThat(adminDetails.isCredentialsNonExpired()).isTrue();
        assertThat(adminDetails.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Usuario inactivo debe tener todas las propiedades de cuenta en false")
    void inactiveUser_ShouldHaveAllAccountPropertiesFalse() {
        assertThat(inactiveDetails.isAccountNonExpired()).isFalse();
        assertThat(inactiveDetails.isAccountNonLocked()).isFalse();
        assertThat(inactiveDetails.isCredentialsNonExpired()).isFalse();
        assertThat(inactiveDetails.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("getUserId y getOrganizationId deben retornar los valores correctos")
    void getters_ShouldReturnCorrectIds() {
        assertThat(adminDetails.getUserId()).isEqualTo(1L);
        assertThat(adminDetails.getOrganizationId()).isEqualTo(10L);
        assertThat(adminDetails.getFullName()).isEqualTo("Admin Demo");
        assertThat(adminDetails.getRole()).isEqualTo(Role.ADMIN);
    }
}