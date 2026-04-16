package com.eam.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrganizationResponse {
    private final Long id;
    private final String name;
    private final String code;
    private final boolean active;
}
