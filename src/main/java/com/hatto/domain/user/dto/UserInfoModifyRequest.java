package com.hatto.domain.user.dto;


import lombok.Getter;

@Getter
public class UserInfoModifyRequest {

    private String preNickname;

    private String nickname;

    private String password;

    private String profileImg;

    private String fcmToken;
}
