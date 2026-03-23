package com.hatto.domain.user;

import com.hatto.common.model.enums.SuccessMessage;
import com.hatto.common.model.response.GlobalResponse;
import com.hatto.domain.user.dto.UserInfoModifyRequest;
import com.hatto.domain.user.dto.UserInfoModifyResponse;
import com.hatto.domain.user.dto.PasswordCheckRequest;
import jakarta.validation.Valid;
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
    @PatchMapping
    public ResponseEntity<GlobalResponse> modifyMemberInfo(
            @RequestHeader("AccessToken") String accessToken,
            @RequestBody UserInfoModifyRequest userInfoModify) {

        // Exception 방지를 위해 안전하게 토큰 문자열 추출
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        UserInfoModifyResponse response = userService.userInfoModifyResponse(token, userInfoModify);

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.USER_INFO_MODIFY_SUCCESS, response));
    }

    //이전 비밀번호 확인
    @GetMapping("/check-password")
    public ResponseEntity<GlobalResponse> prePasswordVerification(
            @RequestHeader("AccessToken") String accessToken,
            @Valid @RequestBody PasswordCheckRequest request) {

        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        Boolean isMatch = userService.prePasswordVerification(token, request.getPassword());

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.PRE_PASSWORD_VERIFICATION_SUCCESS, isMatch));
    }

    //비밀번호 변경
    @PatchMapping("/{loginId}/{email}")
    public ResponseEntity<GlobalResponse> changePassword(
            @RequestHeader("AccessToken") String accessToken,
            @Valid @RequestBody PasswordCheckRequest request) {

        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        Boolean isChange = userService.changePassword(token, request.getPassword());

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.CHANGE_PASSWORD_SUCCESS, isChange));
    }
}