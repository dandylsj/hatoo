package com.hatoo.domain.groups.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class GroupCreateResponse {

    private UUID id;

    private String name;

    private String description;

    private String assignerId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public GroupCreateResponse(UUID id, String name, String description, String assignerId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.assignerId = assignerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
