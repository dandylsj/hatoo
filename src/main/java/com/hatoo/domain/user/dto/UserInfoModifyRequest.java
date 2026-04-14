package com.hatoo.domain.user.dto;


import com.hatoo.domain.groupMember.ProfileImg;
import lombok.Getter;

@Getter
public class UserInfoModifyRequest {

    private String nickname;

    private String password;

    private ProfileImg profileImg;

    private String fcmToken;
}
