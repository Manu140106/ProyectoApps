package com.eam.demo.dto;

import com.eam.demo.entity.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DocumentResponse {
    private final Long id;
    private final String title;
    private final String originalFileName;
    private final String contentType;
    private final Long size;
    private final DocumentStatus status;
    private final Long documentTypeId;
    private final String documentTypeName;
    private final Long ownerUserId;
    private final String ownerName;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
