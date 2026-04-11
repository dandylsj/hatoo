package com.hatoo.domain.groups.dto;

import com.hatoo.domain.groups.Group;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class GroupTokenSameListDto {

    private UUID id;

    private String name;

    private String description;

    private UUID assignerId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public GroupTokenSameListDto(UUID id, String name, String description, UUID assignerId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.assignerId = assignerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static GroupTokenSameListDto from(Group group) {
        return new GroupTokenSameListDto(group.getId(), group.getName(), group.getDescription(), group.getAssignerId(), group.getCreatedAt(), group.getUpdatedAt());

    }
}
