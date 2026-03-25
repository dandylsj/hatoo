package com.hatoo.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoModifyResponse {

    private Long id;

    private String nickname;

    private String profileImg;
}
