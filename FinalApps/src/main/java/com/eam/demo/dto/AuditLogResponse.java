package com.eam.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AuditLogResponse {
    private final Long id;
    private final String action;
    private final String details;
    private final LocalDateTime createdAt;
    private final Long actorUserId;
    private final String actorName;
}
