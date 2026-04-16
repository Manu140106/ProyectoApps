package com.eam.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentTypeRequest {

    @NotBlank
    @Size(max = 120)
    private String name;

    @Size(max = 255)
    private String description;

    private Boolean active;
}
