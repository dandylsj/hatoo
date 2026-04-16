package com.hatoo.domain.groups.dto;

import com.hatoo.domain.groups.Group;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class MyGroupResponse {

    private UUID id;

    private String name;

    private String description;

    private UUID assignerId;

    private boolean isPersonal;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public MyGroupResponse(UUID id, String name, String description, UUID assignerId, boolean isPersonal, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.assignerId = assignerId;
        this.isPersonal = isPersonal;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static MyGroupResponse from(Group group) {
        return new MyGroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getAssignerId(),
                group.isPersonal(),
                group.getCreatedAt(),
                group.getUpdatedAt()
        );
    }
}
