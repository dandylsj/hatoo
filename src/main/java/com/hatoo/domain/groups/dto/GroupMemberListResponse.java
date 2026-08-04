package com.hatoo.domain.groups.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "그룹 멤버 목록 응답")
public class GroupMemberListResponse {

    @Schema(description = "그룹 멤버 목록")
    private List<GroupMemberDto> members;

}