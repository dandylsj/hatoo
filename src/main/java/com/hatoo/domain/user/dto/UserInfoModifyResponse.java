package com.hatoo.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserInfoModifyResponse {

    private UUID id;

    private String nickname;

    private String profileImg;
}
