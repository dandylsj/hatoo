package com.hatoo.domain.groups.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "그룹 초대코드 발급 응답")
public class GroupInviteCodeResponse {

    @Schema(description = "발급된 초대코드")
    private String inviteCode;

    @Schema(description = "초대코드 만료일시")
    private LocalDateTime expiryDate;

}
