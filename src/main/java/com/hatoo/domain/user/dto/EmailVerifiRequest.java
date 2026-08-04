package com.hatoo.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Getter;

@Getter
@Schema(description = "이메일 인증코드 확인 요청")
public class EmailVerifiRequest {

    @Schema(description = "인증코드를 받은 이메일 주소")
    private String email;

    @Schema(description = "이메일로 받은 인증코드")
    private String token;
}
