package com.hatoo.domain.groups.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GroupUpdateNameRequest {

    @NotBlank(message = "그룹 이름은 필수입니다.")
    private String name;
}
