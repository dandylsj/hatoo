package com.hatoo.domain.groups.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "그룹 초대코드 발급 요청")
public class GroupInviteCodeRequest {

    @Schema(description = "초대코드를 발급할 그룹 ID")
    private UUID groupId;

    public UUID getGroupId() {
        return groupId;
    }

}
