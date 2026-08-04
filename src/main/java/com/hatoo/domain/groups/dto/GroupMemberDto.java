package com.hatoo.domain.groups.dto;

import com.hatoo.domain.groupMember.GroupMember;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Schema(description = "그룹 멤버 정보")
public class GroupMemberDto {

    @Schema(description = "사용자 ID")
    private UUID id;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "그룹 내 프로필 색상 코드", example = "PINK")
    private String profileImg;

    // GroupMember 엔티티를 DTO로 변환하는 팩토리 메서드
    public static GroupMemberDto from(GroupMember groupMember) {
        return new GroupMemberDto(
                groupMember.getUser().getId(),
                groupMember.getUser().getEmail(),
                groupMember.getUser().getNickname(),
                groupMember.getProfileImg()
        );
    }
}
