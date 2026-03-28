package com.hatoo.domain.groups.dto;

import com.hatoo.domain.groups.Group;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class MyGroupResponse {

    private UUID id;

    private String name;

    private String description;

    private String assignerId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public MyGroupResponse(Group group) {
        this.id = group.getId();
        this.name = group.getName();
        this.description = group.getDescription();
        this.assignerId = group.getAssignerId();
        this.createdAt = group.getCreatedAt();
        this.updatedAt = group.getUpdatedAt();
    }
}
