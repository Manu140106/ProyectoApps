package com.eam.demo.controller;

import com.eam.demo.dto.AuthRequest;
import com.eam.demo.dto.AuthResponse;
import com.eam.demo.dto.OrganizationResponse;
import com.eam.demo.dto.RegisterOrganizationRequest;
import com.eam.demo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion", description = "Registro de organizacion, login y consulta de organizacion actual")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register-organization")
    @Operation(summary = "Registrar organizacion", description = "Crea una organizacion y su usuario administrador inicial")
    public ResponseEntity<OrganizationResponse> registerOrganization(@Valid @RequestBody RegisterOrganizationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerOrganization(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion", description = "Autentica un usuario y retorna token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/organization")
    @Operation(summary = "Obtener organizacion actual", description = "Retorna los datos de la organizacion del usuario autenticado")
    public ResponseEntity<OrganizationResponse> currentOrganization() {
        return ResponseEntity.ok(authService.currentOrganization());
    }
}
