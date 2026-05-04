package com.hatoo.domain.auth.dto;

import lombok.Getter;

@Getter
public class RefreshTokenReissuanceRequest {

    private String refreshToken;
}
