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

        // 2. 유저가 속한 그룹 리스트를 DTO로 변환하여 반환
        return user.getGroups().stream()
                .map(MyGroupResponse::from)
                .collect(Collectors.toList());
    }

    //그룹 생성
    @Transactional
    public GroupCreateResponse groupCreateResponse(GroupCreateRequest request) {

        Group group = new Group(
                request.getName(),
                request.getDescription()
        );

        groupRepository.save(group);

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
    @Transactional
    public GroupMemberListResponse groupMemberListResponse(UUID groupId) {

        groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        // 1. 해당 그룹에 속한 유저 리스트 조회
        List<User> users = userRepository.findAllByGroupsId(groupId);

        // 2. User 엔티티 리스트를 GroupMemberDto 리스트로 변환
        List<GroupMemberDto> memberDtos = users.stream()
                .map(GroupMemberDto::from)
                .collect(Collectors.toList());

        // 3. DTO에 감싸서 반환 (총 멤버 수와 함께 반환)
        return new GroupMemberListResponse(memberDtos.size(), memberDtos);
    }

    //그룹 참여
    @Transactional
    public boolean joinGroupApi(String accessToken, UUID groupId) {
        // 1. 토큰 검증 및 유저 로그인 ID 추출
        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        // 2. 유저 조회
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        // 3. 이미 해당 그룹에 참여 중인지 확인
        boolean alreadyJoined = user.getGroups().stream()
                .anyMatch(g -> g.getId().equals(groupId));
        if (alreadyJoined) {
            throw new CustomException(ErrorMessage.ALREADY_JOINED_GROUP);
        }

        // 4. 참여하려는 그룹 조회
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        // 5. 유저를 그룹에 추가
        return user.assignGroup(group);
    }

    //그룹 초대코드 생성
    @Transactional
    public GroupInviteCodeResponse inviteCodeAPi(GroupInviteCodeRequest request) {

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        // 그룹 초대 코드 생성 및 유효기간 설정
        String inviteCode = generateInviteCode();
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);

        group.updateInviteCode(inviteCode, expiryDate);

        return new GroupInviteCodeResponse(inviteCode, expiryDate);
    }

    private String generateInviteCode() {
        int length = 4;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder inviteCode = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            inviteCode.append(characters.charAt(random.nextInt(characters.length())));
        }
        return inviteCode.toString();
    }
}
