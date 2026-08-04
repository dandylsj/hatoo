package com.hatoo.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "로그인 요청")
public class LoginRequest {

    @Schema(description = "로그인 아이디")
    @NotBlank(message = "이메일을 입력해주세요")
    private String loginId;

    @Schema(description = "로그인 비밀번호")
    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;

}
