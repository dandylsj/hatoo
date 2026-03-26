package com.hatoo.domain.groups;

import com.hatoo.common.model.enums.SuccessMessage;
import com.hatoo.common.model.response.GlobalResponse;
import com.hatoo.domain.groups.dto.MyGroupResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Group", description = "그룹 관련 API")
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    public ResponseEntity<GlobalResponse> myGroupInfoApi(@RequestHeader("AccessToken") String accessToken) {

        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        MyGroupResponse myGroup = groupService.myGroupInfoResponse(accessToken);

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.GROUP_INFO_SUCCESS,myGroup));
    }
}
