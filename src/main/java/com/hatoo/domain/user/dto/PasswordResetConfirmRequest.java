package com.hatoo.domain.user.dto;

import lombok.Getter;

@Getter
public class PasswordResetConfirmRequest {

    private String email;
    private String token;
    private String newPassword;
}
