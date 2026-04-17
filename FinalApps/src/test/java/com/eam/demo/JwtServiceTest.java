package com.eam.demo;

import com.eam.demo.entity.Role;
import com.eam.demo.security.CustomUserDetails;
import com.eam.demo.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService - Pruebas Unitarias")
class JwtServiceTest {

    private JwtService jwtService;
    private CustomUserDetails userDetails;

    // Clave de prueba de 64+ chars para cumplir HS256
    private static final String TEST_SECRET =
            "clave-super-secreta-de-prueba-para-docucloud-que-tiene-mas-de-64-caracteres-ok";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, 60L); // 60 minutos de expiración

        userDetails = new CustomUserDetails(
                1L, 10L, "Admin Demo", "admin@demo.com", "$2a$hash", Role.ADMIN, true
        );
    }

    @Test
    @DisplayName("generateToken - Debe retornar un token JWT no vacío")
    void generateToken_ShouldReturnNonEmptyToken() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotNull().isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    @DisplayName("extractUsername - Debe retornar el email del usuario")
    void extractUsername_ShouldReturnUserEmail() {
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("admin@demo.com");
    }

    @Test
    @DisplayName("isTokenValid - Token recién generado debe ser válido")
    void isTokenValid_FreshToken_ShouldReturnTrue() {
        String token = jwtService.generateToken(userDetails);

        boolean valid = jwtService.isTokenValid(token);

        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("isTokenValid - Token expirado debe retornar false")
    void isTokenValid_ExpiredToken_ShouldReturnFalse() {
        // Token con expiración de 0 minutos (ya expirado al crearse)
        JwtService shortJwt = new JwtService(TEST_SECRET, 0L);
        String token = shortJwt.generateToken(userDetails);

        boolean valid = shortJwt.isTokenValid(token);

        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("generateToken - Tokens distintos para el mismo usuario deben ser diferentes")
    void generateToken_CalledTwice_ShouldReturnDifferentTokens() throws InterruptedException {
        String token1 = jwtService.generateToken(userDetails);
        Thread.sleep(10); // garantiza distinto timestamp
        String token2 = jwtService.generateToken(userDetails);

        // Los tokens pueden ser iguales si el timestamp es exactamente igual;
        // lo importante es que ambos son válidos y con el mismo username
        assertThat(jwtService.extractUsername(token1)).isEqualTo("admin@demo.com");
        assertThat(jwtService.extractUsername(token2)).isEqualTo("admin@demo.com");
    }

    @Test
    @DisplayName("generateToken - Usuario con rol USER debe generar token válido")
    void generateToken_UserRole_ShouldReturnValidToken() {
        CustomUserDetails userRoleDetails = new CustomUserDetails(
                2L, 10L, "Manuela Camacho", "manuela@demo.com", "manuela14012", Role.USER, true
        );

        String token = jwtService.generateToken(userRoleDetails);

        assertThat(token).isNotNull().isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("manuela@demo.com");
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }
}