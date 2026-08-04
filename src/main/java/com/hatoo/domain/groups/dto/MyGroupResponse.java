package com.hatoo.domain.groups.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hatoo.domain.groups.Group;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Schema(description = "내 그룹 정보 응답")
public class MyGroupResponse {

    @Schema(description = "그룹 ID")
    private UUID id;

    @Schema(description = "그룹 이름")
    private String name;

    @Schema(description = "그룹 설명")
    private String description;

    @Schema(description = "그룹장(방장) 사용자 ID")
    private UUID assignerId;

    @Schema(description = "개인용(1인) 그룹 여부")
    @JsonProperty("isPersonal")
    private boolean isPersonal;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
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