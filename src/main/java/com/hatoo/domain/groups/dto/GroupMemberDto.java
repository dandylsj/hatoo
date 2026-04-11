package com.hatoo.domain.groups.dto;

import com.hatoo.domain.groupMember.GroupMember;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class GroupMemberDto {

    private UUID id;
    private String email;
    private String nickname;
    private String profileImg; // GroupMember에서 가져오는 그룹별 색상

    // GroupMember 엔티티를 DTO로 변환하는 팩토리 메서드
    public static GroupMemberDto from(GroupMember groupMember) {
        return new GroupMemberDto(
                groupMember.getUser().getId(),
                groupMember.getUser().getEmail(),
                groupMember.getUser().getNickname(),
                groupMember.getProfileImg() != null ? groupMember.getProfileImg().name() : null
        );
    }
}
