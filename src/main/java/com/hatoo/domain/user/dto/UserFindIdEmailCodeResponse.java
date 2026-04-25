package com.hatoo.domain.user.dto;

import lombok.Getter;

@Getter
public class UserFindIdEmailCodeResponse {

    private final String loginId;

    public UserFindIdEmailCodeResponse(String loginId) {
        this.loginId = loginId;
    }
}
