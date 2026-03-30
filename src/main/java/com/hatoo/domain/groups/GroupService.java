package com.hatoo.domain.groups;

import com.hatoo.common.exception.CustomException;
import com.hatoo.common.exception.ErrorMessage;
import com.hatoo.common.util.JwtUtil;
import com.hatoo.domain.groups.dto.*;

import com.hatoo.domain.user.User;
import com.hatoo.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    //내가 속한 그룹 조회
    @Transactional(readOnly = true)
    public List<MyGroupResponse> myGroupInfoResponse(String accessToken) {

        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        return user.getGroups().stream()
                .map(MyGroupResponse::from)
                .collect(Collectors.toList());
    }

    //그룹 생성
    @Transactional
    public GroupCreateResponse groupCreateResponse(String accessToken, GroupCreateRequest request) {

        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        Group group = new Group(
                request.getName(),
                request.getDescription(),
                user.getId()
        );
        groupRepository.save(group);
        user.assignGroup(group);

        return new GroupCreateResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getAssignerId(),
                group.getCreatedAt(),
                group.getUpdatedAt()
        );
    }

    //그룹 멤버 리스트 조회
    @Transactional(readOnly = true)
    public GroupMemberListResponse groupMemberListResponse(UUID groupId) {

        groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        List<User> users = userRepository.findAllByGroupsId(groupId);

        List<GroupMemberDto> memberDtos = users.stream()
                .map(GroupMemberDto::from)
                .collect(Collectors.toList());

        return new GroupMemberListResponse(memberDtos.size(), memberDtos);
    }

    //그룹 참여
    @Transactional
    public boolean joinGroupApi(String accessToken, UUID groupId) {

        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        boolean alreadyJoined = user.getGroups().stream()
                .anyMatch(g -> g.getId().equals(groupId));
        if (alreadyJoined) {
            throw new CustomException(ErrorMessage.ALREADY_JOINED_GROUP);
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        return user.assignGroup(group);
    }

    //그룹 초대코드 생성
    @Transactional
    public GroupInviteCodeResponse inviteCodeAPi(GroupInviteCodeRequest request) {

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        String inviteCode = generateInviteCode();
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);

        group.updateInviteCode(inviteCode, expiryDate);

        return new GroupInviteCodeResponse(inviteCode, expiryDate);
    }

    //그룹 삭제
    @Transactional
    public boolean deleteGroup(String accessToken) {

        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        Group group = groupRepository.findByAssignerId(user.getId())
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        groupRepository.delete(group);
        return true;
    }

    private String generateInviteCode() {
        int length = 4;
        String characters = "0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
}
