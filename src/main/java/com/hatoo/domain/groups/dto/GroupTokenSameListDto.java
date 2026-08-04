package com.hatoo.domain.groups.dto;

import com.hatoo.domain.groups.Group;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Schema(description = "초대코드로 조회한 그룹 정보")
public class GroupTokenSameListDto {

    @Schema(description = "그룹 ID")
    private UUID id;

    @Schema(description = "그룹 이름", example = "하투네")
    private String name;

    @Schema(description = "그룹 설명", example = "우리집 청소 담당표")
    private String description;

    @Schema(description = "그룹장(방장) 사용자 ID")
    private UUID assignerId;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
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
