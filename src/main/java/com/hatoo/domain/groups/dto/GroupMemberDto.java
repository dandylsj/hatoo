package com.hatoo.domain.groups.dto;

import com.hatoo.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class GroupMemberDto {

    private UUID id;
    private String email;
    private String nickname;
    private String profileImg;

    // User 엔티티를 DTO로 변환하는 팩토리 메서드
    public static GroupMemberDto from(User user) {
        return new GroupMemberDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImg()
        );
    }
}