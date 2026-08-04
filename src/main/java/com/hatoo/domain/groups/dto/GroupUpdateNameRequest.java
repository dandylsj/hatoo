package com.hatoo.domain.groups.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "그룹 이름 수정 요청")
public class GroupUpdateNameRequest {

    @Schema(description = "변경할 그룹 이름")
    @NotBlank(message = "그룹 이름은 필수입니다.")
    private String name;
}
