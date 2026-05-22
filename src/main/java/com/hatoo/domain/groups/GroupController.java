package com.hatoo.domain.groups;

import com.hatoo.domain.groups.dto.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Group", description = "그룹 관련 API")
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "내가 속한 그룹 조회", description = "로그인한 유저가 속한 그룹 정보를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<MyGroupResponse>> myGroupInfoApi(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(groupService.myGroupInfoResponse(token));
    }

    @Operation(summary = "그룹 생성", description = "새로운 그룹을 생성합니다.")
    @PostMapping
    public ResponseEntity<GroupCreateResponse> createGroup(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @RequestBody GroupCreateRequest request) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(groupService.groupCreateResponse(token, request));
    }

    @Operation(summary = "그룹의 모든 멤버 조회", description = "그룹 내의 모든 멤버를 조회합니다.")
    @GetMapping("/members/{groupId}")
    public ResponseEntity<GroupMemberListResponse> getGroupMembers(@PathVariable UUID groupId) {
        return ResponseEntity.ok(groupService.groupMemberListResponse(groupId));
    }

    @Operation(summary = "그룹 참여", description = "그룹에 참여합니다.")
    @PostMapping("/add-user/{groupId}/{token}")
    public ResponseEntity<Boolean> joinGroup(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID groupId,
            @PathVariable String token) {
        String userToken = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(groupService.joinGroupApi(userToken, groupId, token));
    }

    @Operation(summary = "그룹 초대코드 생성", description = "그룹 초대코드를 생성합니다.")
    @PostMapping("/token")
    public ResponseEntity<GroupInviteCodeResponse> inviteCode(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @RequestBody GroupInviteCodeRequest request) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(groupService.inviteCodeAPi(token, request));
    }

    @Operation(summary = "그룹 삭제", description = "방장이 그룹을 삭제합니다.")
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Boolean> deleteGroup(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID groupId) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(groupService.deleteGroup(token, groupId));
    }

    @Operation(summary = "그룹 탈퇴", description = "현재 그룹을 탈퇴합니다.")
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Boolean> leaveGroup(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID groupId) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(groupService.leaveGroup(token, groupId));
    }

    @Operation(summary = "그룹 멤버 내보내기", description = "그룹장이 멤버를 탈퇴 시킵니다.")
    @DeleteMapping("/{groupId}/{memberId}")
    public ResponseEntity<Boolean> forcedExpulsionOfMembers(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID groupId, @PathVariable UUID memberId) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(groupService.forcedLeaveGroup(token, groupId, memberId));
    }

    @Operation(summary = "초대코드로 그룹 조회", description = "초대코드로 보이는 그룹 전체를 조회합니다.")
    @GetMapping("/{token}")
    public ResponseEntity<List<GroupTokenSameListDto>> tokenGroupList(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable String token) {
        String authToken = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(groupService.tokenGroupListApi(authToken, token));
    }

    @Operation(summary = "그룹 참여 시 프로필 이미지 선택", description = "그룹 참여 시 팀원이 프로필 이미지를 선택합니다.")
    @PatchMapping("/{groupId}/{memberId}")
    public ResponseEntity<Boolean> profileImgSelect(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @RequestBody GroupJoinProfileRequest request, @PathVariable UUID groupId) {
        String authToken = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(groupService.profileImgSelectApi(authToken, request, groupId));
    }

    @Operation(summary = "그룹 이름 수정", description = "방장이 그룹 이름을 수정합니다.")
    @PatchMapping("/{groupId}/name")
    public ResponseEntity<Boolean> updateGroupName(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID groupId,
            @Valid @RequestBody GroupUpdateNameRequest request) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(groupService.updateGroupName(token, groupId, request));
    }

    @Operation(summary = "그룹별 알림 설정 조회", description = "특정 그룹의 알림 설정을 조회합니다.")
    @GetMapping("/{groupId}/alarm")
    public ResponseEntity<GroupAlarmSettingResponse> getGroupAlarmSetting(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID groupId) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(groupService.getGroupAlarmSetting(token, groupId));
    }

    @Operation(summary = "그룹별 알림 설정 수정", description = "특정 그룹의 알림 설정을 수정합니다. null인 항목은 변경되지 않습니다. 응답으로 변경된 실제 설정값을 반환합니다.")
    @PatchMapping("/{groupId}/alarm")
    public ResponseEntity<GroupAlarmSettingResponse> updateGroupAlarmSetting(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID groupId,
            @RequestBody GroupAlarmSettingRequest request) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(groupService.updateGroupAlarmSetting(token, groupId, request));
    }
}
