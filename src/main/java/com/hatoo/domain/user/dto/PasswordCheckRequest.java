package com.hatoo.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Schema(description = "현재 비밀번호 확인 요청")
public class PasswordCheckRequest {

    @Schema(description = "현재 비밀번호")
    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
}