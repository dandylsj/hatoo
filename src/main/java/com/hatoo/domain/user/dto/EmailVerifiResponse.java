package com.hatoo.domain.user.dto;

import lombok.Getter;

@Getter
public class EmailVerifiResponse {

    private String email;

    private String token;
}
