package com.eam.demo.dto;

import com.eam.demo.entity.DocumentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentStatusUpdateRequest {

    @NotNull
    private DocumentStatus status;

    @Size(max = 255)
    private String details;
}
