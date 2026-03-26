package com.hatoo.domain.groups.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GroupCreateResponse {

    private Long id;

    private String name;

    private String description;

    private String assignerId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public GroupCreateResponse(Long id, String name, String description, String assignerId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.assignerId = assignerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
