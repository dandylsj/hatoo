package com.hatoo.domain.groups.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Schema(description = "그룹 생성 응답")
public class GroupCreateResponse {

    @Schema(description = "그룹 ID")
    private UUID id;

    @Schema(description = "그룹 이름")
    private String name;

    @Schema(description = "그룹 설명")
    private String description;

    @Schema(description = "그룹장(방장) 사용자 ID")
    private UUID assignerId;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    public GroupCreateResponse(UUID id, String name, String description, UUID assignerId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.assignerId = assignerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
