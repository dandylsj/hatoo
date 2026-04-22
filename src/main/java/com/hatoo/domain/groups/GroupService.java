package com.hatoo.domain.groups;

import com.hatoo.common.exception.CustomException;
import com.hatoo.common.exception.ErrorMessage;
import com.hatoo.common.util.JwtUtil;
import com.hatoo.domain.groupAlarmSetting.GroupAlarmSetting;
import com.hatoo.domain.groupAlarmSetting.GroupAlarmSettingRepository;
import com.hatoo.domain.groupMember.GroupMember;
import com.hatoo.domain.groupMember.GroupMemberRepository;
import com.hatoo.domain.groups.dto.*;

import com.hatoo.domain.task.Task;
import com.hatoo.domain.task.TaskRepository;
import com.hatoo.domain.user.User;
import com.hatoo.domain.user.UserRepository;
import com.hatoo.domain.alarm.FcmService;
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
    private final GroupAlarmSettingRepository groupAlarmSettingRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final FcmService fcmService;
    private final TaskRepository taskRepository;

    // 내가 속한 그룹 조회
    @Transactional(readOnly = true)
    public List<MyGroupResponse> myGroupInfoResponse(String accessToken) {

        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        List<GroupMember> groupMembers = groupMemberRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return groupMembers.stream()
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
        //1. Group 에 저장
        Group group = new Group(
                request.getName(),
                request.getDescription(),
                user.getId()
        );
        groupRepository.save(group);

        // 그룹 생성자를 GroupMember로 등록
        GroupMember groupMember = new GroupMember(user, group, user.getProfileImg(), false);
        groupMemberRepository.save(groupMember);

        // 그룹 알람 설정 기본값 생성 (전부 ON)
        GroupAlarmSetting alarmSetting = new GroupAlarmSetting(user.getId(), group.getId());
        groupAlarmSettingRepository.save(alarmSetting);

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

        List<GroupMember> members = groupMemberRepository.findByGroupIdOrderByCreatedAtAsc(groupId);

        List<GroupMemberDto> memberDtos = members.stream()
                .map(GroupMemberDto::from)
                .collect(Collectors.toList());

        return new GroupMemberListResponse(memberDtos);
    }

    // 그룹 참여
    @Transactional
    public boolean joinGroupApi(String accessToken, UUID groupId, String token) {

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

        // 6. GroupMember 생성 및 저장
        GroupMember groupMember = new GroupMember(user, group, user.getProfileImg(), false);
        groupMemberRepository.save(groupMember);

        // 7. 그룹 알람 설정 기본값 생성 (전부 ON)
        GroupAlarmSetting alarmSetting = new GroupAlarmSetting(user.getId(), group.getId());
        groupAlarmSettingRepository.save(alarmSetting);

        // 8. 새 멤버 참여 알림 전송 (그룹 전체에게)
        fcmService.sendNewMember(groupId, group.getName(), user.getNickname());

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

        // group_members + group_alarm_settings 전체 삭제 후 그룹 삭제
        List<GroupMember> members = groupMemberRepository.findByGroupIdOrderByCreatedAtAsc(groupId);
        groupMemberRepository.deleteAll(members);
        groupAlarmSettingRepository.deleteByGroupId(groupId);

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

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        GroupMember groupMember = groupMemberRepository.findByUserIdAndGroupId(user.getId(), groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_IN_GROUP));

        // 그룹내의 할 일 모두 삭제
        List<Task> myTasksInGroup = taskRepository.findByAssigneesIdAndGroupsId(user.getId(), group.getId());
        for (Task task : myTasksInGroup) {
            task.getAssignees().clear();
            task.getGroups().clear();
            taskRepository.delete(task);
        }
        groupMemberRepository.delete(groupMember);
        groupAlarmSettingRepository.deleteByUserIdAndGroupId(user.getId(), groupId);

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
        groupAlarmSettingRepository.deleteByUserIdAndGroupId(memberId, groupId);

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

    // 프로필 이미지 선택 (그룹 참여 시)
    @Transactional
    public Boolean profileImgSelectApi(String accessToken, GroupJoinProfileRequest request, UUID groupId) {

        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        GroupMember groupMember = groupMemberRepository.findByUserIdAndGroupId(user.getId(), groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_IN_GROUP));

        groupMember.updateProfileImg(request.getProfileImg());

        return true;
    }

    // 그룹 알람 설정 조회
    @Transactional(readOnly = true)
    public GroupAlarmSettingResponse getGroupAlarmSetting(String accessToken, UUID groupId) {

        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        GroupAlarmSetting setting = groupAlarmSettingRepository
                .findByUserIdAndGroupId(user.getId(), groupId)
                .orElse(null);

        return setting != null
                ? GroupAlarmSettingResponse.from(setting)
                : GroupAlarmSettingResponse.defaultEnabled(groupId);
    }

    // 그룹 알람 설정 수정
    @Transactional
    public GroupAlarmSettingResponse updateGroupAlarmSetting(String accessToken, UUID groupId, GroupAlarmSettingRequest request) {

        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        GroupAlarmSetting setting = groupAlarmSettingRepository
                .findByUserIdAndGroupId(user.getId(), groupId)
                .orElseGet(() -> {
                    GroupAlarmSetting newSetting = new GroupAlarmSetting(user.getId(), groupId);
                    return groupAlarmSettingRepository.save(newSetting);
                });

        setting.update(
                request.getIsGroupNotiEnabled(),
                request.getIsNewTaskNotiEnabled(),
                request.getIsNewMemberNotiEnabled(),
                request.getIsTaskCompleteNotiEnabled()
        );

        return GroupAlarmSettingResponse.from(setting);
    }

    // 초대코드 생성 유틸
    private String generateInviteCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
