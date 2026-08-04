package com.hatoo.domain.groups.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "그룹 생성 요청")
public class GroupCreateRequest {

    @Schema(description = "그룹 이름")
    private String name;

    @Schema(description = "그룹 설명")
    private String description;

}
