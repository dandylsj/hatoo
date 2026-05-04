package com.hatoo.domain.user.dto;

import lombok.Getter;

@Getter
public class PasswordResetSendRequest {

    private String loginId;
    private String email;
}
