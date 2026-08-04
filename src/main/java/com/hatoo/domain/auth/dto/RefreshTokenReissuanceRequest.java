package com.hatoo.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "액세스 토큰 재발급 요청")
public class RefreshTokenReissuanceRequest {

    @Schema(description = "리프레시 토큰")
    private String refreshToken;
}
