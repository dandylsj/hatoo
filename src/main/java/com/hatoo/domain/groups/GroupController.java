package com.hatoo.domain.groups;

import com.hatoo.common.model.response.GlobalResponse;
import com.hatoo.domain.groupMember.ProfileImg;
import com.hatoo.domain.groups.dto.*;
import com.hatoo.domain.groups.dto.GroupTokenSameListDto;
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
    public ResponseEntity<GlobalResponse<List<MyGroupResponse>>> myGroupInfoApi(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        List<MyGroupResponse> myGroups = groupService.myGroupInfoResponse(token);
        return ResponseEntity.ok(GlobalResponse.success(myGroups));
    }

    @Operation(summary = "그룹생성", description = "새로운 그룹을 생성합니다.")
    @PostMapping
    public ResponseEntity<GlobalResponse<GroupCreateResponse>> createGroup(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @RequestBody GroupCreateRequest request) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        GroupCreateResponse group = groupService.groupCreateResponse(token, request);
        return ResponseEntity.ok(GlobalResponse.success(group));
    }

    @Operation(summary = "그룹의 모든 멤버 조회", description = "그룹 내의 모든 멤버를 조회합니다.")
    @GetMapping("/members/{groupId}")
    public ResponseEntity<GlobalResponse<GroupMemberListResponse>> getGroupMembers(@PathVariable UUID groupId) {
        GroupMemberListResponse members = groupService.groupMemberListResponse(groupId);
        return ResponseEntity.ok(GlobalResponse.success(members));
    }

    @Operation(summary = "그룹참여", description = "그룹에 참여합니다. profileImg: RED, BLUE, GREEN, YELLOW, PURPLE 중 선택")
    @PostMapping("/add-user/{groupId}/{token}")
    public ResponseEntity<GlobalResponse<Boolean>> joinGroup(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID groupId,
            @PathVariable String token) {
        String userToken = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        boolean result = groupService.joinGroupApi(userToken, groupId, token);
        return ResponseEntity.ok(GlobalResponse.success(result));
    }

    @Operation(summary = "그룹 초대코드 생성", description = "그룹 초대코드를 생성합니다.")
    @PostMapping("/token")
    public ResponseEntity<GlobalResponse<GroupInviteCodeResponse>> inviteCode(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @RequestBody GroupInviteCodeRequest request) {
        GroupInviteCodeResponse response = groupService.inviteCodeAPi(accessToken, request);
        return ResponseEntity.ok(GlobalResponse.success(response));
    }

    @Operation(summary = "그룹 삭제", description = "방장이 그룹을 삭제합니다.")
    @DeleteMapping("/{groupId}")
    public ResponseEntity<GlobalResponse<Boolean>> deleteGroup(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID groupId) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        boolean result = groupService.deleteGroup(token, groupId);
        return ResponseEntity.ok(GlobalResponse.success(result));
    }

    @Operation(summary = "그룹 탈퇴", description = "현재 그룹을 탈퇴합니다.")
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<GlobalResponse<Boolean>> leaveGroup(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID groupId) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        boolean result = groupService.leaveGroup(token, groupId);
        return ResponseEntity.ok(GlobalResponse.success(result));
    }

    @Operation(summary = "그룹 맴버 내보내기", description = "그룹장이 멤버를 탈퇴 시킵니다.")
    @DeleteMapping("/{groupId}/{memberId}")
    public ResponseEntity<GlobalResponse<Boolean>> forcedExpulsionOfMembers(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID groupId, @PathVariable UUID memberId) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        boolean result = groupService.forcedLeaveGroup(token, groupId, memberId);
        return ResponseEntity.ok(GlobalResponse.success(result));
    }

    @Operation(summary = "초대코드 생성 후 해당되는 그룹 전체 조회", description = "초대코드로 보이는 그룹 전체를 조회")
    @GetMapping("/{token}")
    public ResponseEntity<GlobalResponse<List<GroupTokenSameListDto>>> tokenGroupList(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable String token) {
        String authToken = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        List<GroupTokenSameListDto> result = groupService.tokenGroupListApi(authToken, token);
        return ResponseEntity.ok(GlobalResponse.success(result));
    }

    @Operation(summary = "그룹에 참여하는 팀원 프로필 이미지 선택", description = "그룹 참여시 팀원이 프로필 이미지를 선택하는 로직")
    @PatchMapping("/{groupId}/{memberId}")
    public ResponseEntity<GlobalResponse<Boolean>> profileImgSelect(
            @Parameter(hidden = true) @RequestParam("Authorization") String accessToken,
            @RequestBody GroupJoinProfileRequest request, @PathVariable UUID groupId) {

        String authToken = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        Boolean result = groupService.profileImgSelectApi(authToken, request, groupId);

        return ResponseEntity.ok(GlobalResponse.success(result));
    }
}
