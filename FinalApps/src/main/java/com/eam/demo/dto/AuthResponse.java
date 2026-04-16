package com.eam.demo.dto;

import com.eam.demo.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private final String token;
    private final String tokenType;
    private final Long userId;
    private final Long organizationId;
    private final String fullName;
    private final String email;
    private final Role role;
}
