package com.eam.demo;

import com.eam.demo.controller.UserController;
import com.eam.demo.dto.UserRequest;
import com.eam.demo.dto.UserResponse;
import com.eam.demo.entity.Role;
import com.eam.demo.service.UserService;
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
@DisplayName("UserController - Pruebas Unitarias")
class UserControllerTest {

    @Mock private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    @DisplayName("LIST - Debe responder OK")
    void list_ShouldReturnOk() {
        List<UserResponse> responseBody = List.of(new UserResponse(1L, "Laura", "laura@demo.com", Role.USER, true, 1L));
        when(userService.listByCurrentOrganization()).thenReturn(responseBody);

        assertThat(userController.list().getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userController.list().getBody()).isEqualTo(responseBody);
    }

    @Test
    @DisplayName("CREATE - Debe responder CREATED")
    void create_ShouldReturnCreated() {
        UserRequest request = new UserRequest();
        UserResponse responseBody = new UserResponse(1L, "Laura", "laura@demo.com", Role.USER, true, 1L);
        when(userService.create(request)).thenReturn(responseBody);

        assertThat(userController.create(request).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(userController.create(request).getBody()).isEqualTo(responseBody);
    }

    @Test
    @DisplayName("UPDATE - Debe responder OK")
    void update_ShouldReturnOk() {
        UserRequest request = new UserRequest();
        UserResponse responseBody = new UserResponse(1L, "Laura Actualizada", "laura@demo.com", Role.ADMIN, true, 1L);
        when(userService.update(1L, request)).thenReturn(responseBody);

        assertThat(userController.update(1L, request).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userController.update(1L, request).getBody()).isEqualTo(responseBody);
    }
}