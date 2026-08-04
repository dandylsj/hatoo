package com.hatoo.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "회원정보 수정 요청 (null인 항목은 변경하지 않음)")
public class UserInfoModifyRequest {

    @Schema(description = "변경할 닉네임")
    private String nickname;

    @Schema(description = "변경할 비밀번호")
    private String password;

    @Schema(description = "변경할 프로필 이미지 URL")
    private String profileImg;
}
