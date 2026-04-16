package com.eam.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentTypeResponse {
    private final Long id;
    private final String name;
    private final String description;
    private final boolean active;
    private final Long organizationId;
}
