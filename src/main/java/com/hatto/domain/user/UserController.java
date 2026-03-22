package com.hatto.domain.user;

import com.hatto.common.model.enums.SuccessMessage;
import com.hatto.common.model.response.GlobalResponse;
import com.hatto.domain.user.dto.UserInfoModifyRequest;
import com.hatto.domain.user.dto.UserInfoModifyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //아이디 중복 확인
    @GetMapping("/check-login-id")
    public ResponseEntity<GlobalResponse> checkLoginId(@RequestParam String loginId) {

        Boolean checked = userService.checkLoginIdApi(loginId);

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.USER_CHECK_LOGIN_ID_SUCCESS, checked));
    }

    //닉네임 중복 확인
    @GetMapping("/check-nickname")
    public ResponseEntity<GlobalResponse> checkNickname(@RequestParam String nickname) {

        Boolean checkNickName = userService.checkNicknameApi(nickname);

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.USER_CHECK_LOGIN_ID_SUCCESS, checkNickName));
    }

    //유저 정보 수정
    @PatchMapping()
    public ResponseEntity<GlobalResponse> modifyMemberInfo(
            @RequestHeader("AccessToen") String AccessToken,
            @RequestBody UserInfoModifyRequest userInfoModify) {
            
        // "Bearer " 접두사 제거
        String token = AccessToken.substring(7);

        // UserService에 토큰과 수정 정보를 넘겨서 처리
        UserInfoModifyResponse response = userService.userInfoModifyResponse(token, userInfoModify);

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.USER_INFO_MODIFY_SUCCESS, response));
    }
}
