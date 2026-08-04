package com.hatoo.domain.groups.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "그룹 참여 프로필 색상 선택 요청")
public class GroupJoinProfileRequest {

    @Schema(description = "그룹 내에서 사용할 프로필 색상 코드 (예: PINK)", example = "PINK")
    private String profileImg;

}
