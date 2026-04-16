package com.eam.demo.dto;

import com.eam.demo.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {
    private final Long id;
    private final String fullName;
    private final String email;
    private final Role role;
    private final boolean active;
    private final Long organizationId;
}
