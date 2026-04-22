package com.eam.demo;

import com.eam.demo.controller.DocumentTypeController;
import com.eam.demo.dto.DocumentTypeRequest;
import com.eam.demo.dto.DocumentTypeResponse;
import com.eam.demo.service.DocumentTypeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentTypeController - Pruebas Unitarias")
class DocumentTypeControllerTest {

    @Mock private DocumentTypeService documentTypeService;

    @InjectMocks
    private DocumentTypeController documentTypeController;

    @Test
    @DisplayName("LIST - Debe responder OK")
    void list_ShouldReturnOk() {
        List<DocumentTypeResponse> responseBody = List.of(new DocumentTypeResponse(1L, "Contrato", "Desc", true, 1L));
        when(documentTypeService.list()).thenReturn(responseBody);

        assertThat(documentTypeController.list().getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(documentTypeController.list().getBody()).isEqualTo(responseBody);
    }

    @Test
    @DisplayName("CREATE - Debe responder CREATED")
    void create_ShouldReturnCreated() {
        DocumentTypeRequest request = new DocumentTypeRequest();
        DocumentTypeResponse responseBody = new DocumentTypeResponse(1L, "Contrato", "Desc", true, 1L);
        when(documentTypeService.create(request)).thenReturn(responseBody);

        assertThat(documentTypeController.create(request).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(documentTypeController.create(request).getBody()).isEqualTo(responseBody);
    }

    @Test
    @DisplayName("UPDATE - Debe responder OK")
    void update_ShouldReturnOk() {
        DocumentTypeRequest request = new DocumentTypeRequest();
        DocumentTypeResponse responseBody = new DocumentTypeResponse(1L, "Contrato", "Desc", true, 1L);
        when(documentTypeService.update(1L, request)).thenReturn(responseBody);

        assertThat(documentTypeController.update(1L, request).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(documentTypeController.update(1L, request).getBody()).isEqualTo(responseBody);
    }

    @Test
    @DisplayName("DELETE - Debe responder NO_CONTENT")
    void delete_ShouldReturnNoContent() {
        assertThat(documentTypeController.delete(1L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}