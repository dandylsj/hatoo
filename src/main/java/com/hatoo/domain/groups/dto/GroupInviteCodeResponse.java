package com.hatoo.domain.groups.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GroupInviteCodeResponse {

    private String inviteCode;
    private LocalDateTime expiryDate;

}
