package com.hatoo.domain.groups;

import com.hatoo.common.model.enums.SuccessMessage;
import com.hatoo.common.model.response.GlobalResponse;
import com.hatoo.domain.groups.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Group", description = "그룹 관련 API")
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "내가 속한 그룹 조회", description = "로그인한 유저가 속한 그룹 정보를 조회합니다.")
    @GetMapping
    public ResponseEntity<GlobalResponse> myGroupInfoApi(@Parameter(hidden = true) @RequestHeader("Authorization") String accessToken) {

        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        MyGroupResponse myGroup = groupService.myGroupInfoResponse(token);

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.GROUP_INFO_SUCCESS,myGroup));
    }

    @Operation(summary = "그룹생성", description = "새로운 그룹을 생성합니다.")
    @PostMapping
    public ResponseEntity<GlobalResponse> createGroup(@RequestBody GroupCreateRequest request) {

        GroupCreateResponse group = groupService.groupCreateResponse(request);

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.GROUP_CREATE_SUCCESS,group));
    }

    @Operation(summary = "그룹의 모든 멤버 조회", description = "그룹 내의 모든 멤버를 조회합니다.")
    @GetMapping("/members/{groupId}")
    public ResponseEntity getGroupMembers(@PathVariable UUID groupId) {

        return ResponseEntity.ok(groupService.groupMemberListResponse(groupId));
    }

    @Operation(summary = "그룹참여", description = "그룹에 참여합니다.")
    @PostMapping("/members/{groupId}")
    public ResponseEntity<GlobalResponse> joinGroup(@Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
                                    @PathVariable UUID groupId) {

        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        boolean result = groupService.joinGroupApi(token, groupId);

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.GROUP_JOIN_SUCCESS, result));
    }

    @Operation(summary = "그룹 초대코드 생성", description = "그룹 초대코드를 생성합니다.")
    @PostMapping("/token")
    public ResponseEntity<GlobalResponse> inviteCode(@RequestBody GroupInviteCodeRequest request) {

        GroupInviteCodeResponse response = groupService.inviteCodeAPi(request);

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.INVITE_CODE_CREATE_SUCCESS, response));
    }

}
