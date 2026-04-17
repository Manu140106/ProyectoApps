package com.eam.demo;

import com.eam.demo.exception.BadRequestException;
import com.eam.demo.exception.ForbiddenOperationException;
import com.eam.demo.exception.GlobalExceptionHandler;
import com.eam.demo.exception.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler - Pruebas Unitarias")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("NotFoundException debe retornar 404 con mensaje")
    void handleNotFound_ShouldReturn404() {
        NotFoundException ex = new NotFoundException("Documento no encontrado");

        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("status", 404);
        assertThat(response.getBody()).containsEntry("message", "Documento no encontrado");
        assertThat(response.getBody()).containsKey("timestamp");
    }

    @Test
    @DisplayName("BadRequestException debe retornar 400 con mensaje")
    void handleBadRequest_ShouldReturn400() {
        BadRequestException ex = new BadRequestException("Email ya existe");

        ResponseEntity<Map<String, Object>> response = handler.handleBadRequest(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", 400);
        assertThat(response.getBody()).containsEntry("message", "Email ya existe");
    }

    @Test
    @DisplayName("ForbiddenOperationException debe retornar 403 con mensaje")
    void handleForbiddenBusiness_ShouldReturn403() {
        ForbiddenOperationException ex = new ForbiddenOperationException("Operación no permitida");

        ResponseEntity<Map<String, Object>> response = handler.handleForbiddenBusiness(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).containsEntry("status", 403);
        assertThat(response.getBody()).containsEntry("message", "Operación no permitida");
    }

    @Test
    @DisplayName("AccessDeniedException debe retornar 403 con mensaje genérico")
    void handleAccessDenied_ShouldReturn403WithGenericMessage() {
        AccessDeniedException ex = new AccessDeniedException("Access is denied");

        ResponseEntity<Map<String, Object>> response = handler.handleAccessDenied(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).containsEntry("status", 403);
        assertThat(response.getBody()).containsEntry("message", "No tiene permisos para este recurso");
    }

    @Test
    @DisplayName("ConstraintViolationException debe retornar 400")
    void handleConstraintViolation_ShouldReturn400() {
        ConstraintViolationException ex = new ConstraintViolationException("Constraint violated", Set.of());

        ResponseEntity<Map<String, Object>> response = handler.handleConstraintViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", 400);
    }

    @Test
    @DisplayName("Exception genérica debe retornar 500")
    void handleGeneric_ShouldReturn500() {
        Exception ex = new RuntimeException("Error inesperado");

        ResponseEntity<Map<String, Object>> response = handler.handleGeneric(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("status", 500);
        assertThat(response.getBody()).containsEntry("message", "Error interno del servidor");
    }

    @Test
    @DisplayName("Todas las respuestas de error deben incluir campo timestamp")
    void allHandlers_ShouldIncludeTimestamp() {
        ResponseEntity<Map<String, Object>> r1 = handler.handleNotFound(new NotFoundException("x"));
        ResponseEntity<Map<String, Object>> r2 = handler.handleBadRequest(new BadRequestException("x"));
        ResponseEntity<Map<String, Object>> r3 = handler.handleGeneric(new Exception("x"));

        assertThat(r1.getBody()).containsKey("timestamp");
        assertThat(r2.getBody()).containsKey("timestamp");
        assertThat(r3.getBody()).containsKey("timestamp");
    }

    @Test
    @DisplayName("Todas las respuestas deben incluir campo error con descripción HTTP")
    void allHandlers_ShouldIncludeErrorDescription() {
        ResponseEntity<Map<String, Object>> r404 = handler.handleNotFound(new NotFoundException("x"));
        ResponseEntity<Map<String, Object>> r400 = handler.handleBadRequest(new BadRequestException("x"));
        ResponseEntity<Map<String, Object>> r500 = handler.handleGeneric(new Exception("x"));

        assertThat(r404.getBody()).containsEntry("error", "Not Found");
        assertThat(r400.getBody()).containsEntry("error", "Bad Request");
        assertThat(r500.getBody()).containsEntry("error", "Internal Server Error");
    }
}
