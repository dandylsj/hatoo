package com.hatoo.domain.groups;

import com.hatoo.common.exception.CustomException;
import com.hatoo.common.exception.ErrorMessage;
import com.hatoo.common.util.JwtUtil;
import com.hatoo.domain.groupMember.GroupMember;
import com.hatoo.domain.groupMember.GroupMemberRepository;
import com.hatoo.domain.groupMember.ProfileImg;
import com.hatoo.domain.groups.dto.*;

import com.hatoo.domain.user.User;
import com.hatoo.domain.user.UserRepository;
import com.hatoo.domain.groups.dto.GroupTokenSameListDto;
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
    private final GroupMemberRepository groupMemberRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // 내가 속한 그룹 조회
    @Transactional(readOnly = true)
    public List<MyGroupResponse> myGroupInfoResponse(String accessToken) {

        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        return user.getGroupMembers().stream()
                .map(gm -> MyGroupResponse.from(gm.getGroup()))
                .collect(Collectors.toList());
    }

    // 그룹 생성
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

        // 그룹 생성자를 GroupMember로 등록
        GroupMember groupMember = new GroupMember(user, group, null);
        groupMemberRepository.save(groupMember);

        return new GroupCreateResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getAssignerId(),
                group.getCreatedAt(),
                group.getUpdatedAt()
        );
    }

    // 그룹 멤버 리스트 조회
    @Transactional(readOnly = true)
    public GroupMemberListResponse groupMemberListResponse(UUID groupId) {

        groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);

        List<GroupMemberDto> memberDtos = members.stream()
                .map(GroupMemberDto::from)
                .collect(Collectors.toList());

        return new GroupMemberListResponse(memberDtos);
    }

    // 그룹 참여
    @Transactional
    public boolean joinGroupApi(String accessToken, UUID groupId, String token, ProfileImg profileImg) {

        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        // 1. 유저 조회
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        // 2. 그룹 조회
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        // 3. 초대 코드 유효성 및 만료 여부 확인
        if (!token.equals(group.getInviteCode()) ||
            (group.getInviteCodeExpiryDate() != null && group.getInviteCodeExpiryDate().isBefore(LocalDateTime.now()))) {
            throw new CustomException(ErrorMessage.NOT_SAME_INVITED);
        }

        // 4. 이미 그룹에 가입되어 있는지 확인
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, user.getId())) {
            throw new CustomException(ErrorMessage.ALREADY_JOINED_GROUP);
        }

        // 5. 그룹 인원 최대 5명 확인
        List<GroupMember> currentMembers = groupMemberRepository.findByGroupId(groupId);
        if (currentMembers.size() >= 5) {
            throw new CustomException(ErrorMessage.GROUP_FULL);
        }

//        // 6. 선택한 색상이 이미 사용 중인지 확인
//        if (groupMemberRepository.existsByGroupIdAndProfileImg(groupId, profileImg)) {
//            throw new CustomException(ErrorMessage.COLOR_ALREADY_TAKEN);
//        }

        // 7. GroupMember 생성 및 저장
        GroupMember groupMember = new GroupMember(user, group, profileImg);
        groupMemberRepository.save(groupMember);

        return true;
    }

    // 그룹 초대코드 생성
    @Transactional
    public GroupInviteCodeResponse inviteCodeAPi(String accessToken, GroupInviteCodeRequest request) {

        jwtUtil.validateToken(accessToken);

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        String inviteCode = generateInviteCode();
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);

        group.updateInviteCode(inviteCode, expiryDate);

        return new GroupInviteCodeResponse(inviteCode, expiryDate);
    }

    // 그룹 삭제
    @Transactional
    public boolean deleteGroup(String accessToken, UUID groupId) {

        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        // 방장 여부 검증
        if (!user.getId().equals(group.getAssignerId())) {
            throw new CustomException(ErrorMessage.NO_DELETE_PERMISSION);
        }

        // group_members 전체 삭제 후 그룹 삭제
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        groupMemberRepository.deleteAll(members);

        groupRepository.delete(group);

        return true;
    }

    // 그룹 탈퇴
    @Transactional
    public boolean leaveGroup(String accessToken, UUID groupId) {

        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        GroupMember groupMember = groupMemberRepository.findByUserIdAndGroupId(user.getId(), groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_IN_GROUP));

        groupMemberRepository.delete(groupMember);

        return true;
    }

    // 그룹 멤버 내보내기
    @Transactional
    public boolean forcedLeaveGroup(String accessToken, UUID groupId, UUID memberId) {

        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        // 그룹장 여부 검증
        if (!user.getId().equals(group.getAssignerId())) {
            throw new CustomException(ErrorMessage.NO_DELETE_PERMISSION);
        }

        GroupMember groupMember = groupMemberRepository.findByUserIdAndGroupId(memberId, groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_IN_GROUP));

        groupMemberRepository.delete(groupMember);

        return true;
    }

    // 초대코드로 해당되는 그룹 전체 조회
    @Transactional
    public List<GroupTokenSameListDto> tokenGroupListApi(String accessToken, String token) {

        jwtUtil.validateToken(accessToken);
        jwtUtil.extractLoginId(accessToken);

        List<Group> groups = groupRepository.findAllByInviteCode(token);

        return groups.stream()
                .map(GroupTokenSameListDto::from)
                .collect(Collectors.toList());
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
