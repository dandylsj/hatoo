package com.hatto.domain.user;

import com.hatto.common.model.enums.SuccessMessage;
import com.hatto.common.model.response.GlobalResponse;
import com.hatto.domain.user.dto.UserInfoModifyRequest;
import com.hatto.domain.user.dto.UserInfoModifyResponse;
import com.hatto.domain.user.dto.PasswordCheckRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "유저 관련 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "아이디 중복 확인", description = "회원가입 시 로그인 아이디의 중복 여부를 확인합니다.")
    @GetMapping("/check-login-id")
    public ResponseEntity<GlobalResponse> checkLoginId(@RequestParam String loginId) {

        Boolean checked = userService.checkLoginIdApi(loginId);

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.USER_CHECK_LOGIN_ID_SUCCESS, checked));
    }

    @Operation(summary = "닉네임 중복 확인", description = "회원가입 시 닉네임의 중복 여부를 확인합니다.")
    @GetMapping("/check-nickname")
    public ResponseEntity<GlobalResponse> checkNickname(@RequestParam String nickname) {

        Boolean checkNickName = userService.checkNicknameApi(nickname);

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.USER_CHECK_LOGIN_ID_SUCCESS, checkNickName));
    }

    @Operation(summary = "유저 정보 수정", description = "로그인한 유저의 정보를 수정합니다.")
    @PatchMapping
    public ResponseEntity<GlobalResponse> modifyMemberInfo(
            @RequestHeader("AccessToken") String accessToken,
            @RequestBody UserInfoModifyRequest userInfoModify) {

        // Exception 방지를 위해 안전하게 토큰 문자열 추출
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        UserInfoModifyResponse response = userService.userInfoModifyResponse(token, userInfoModify);

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.USER_INFO_MODIFY_SUCCESS, response));
    }

    @Operation(summary = "이전 비밀번호 확인", description = "비밀번호 변경 전, 현재 비밀번호가 맞는지 확인합니다.")
    @GetMapping("/check-password")
    public ResponseEntity<GlobalResponse> prePasswordVerification(
            @RequestHeader("AccessToken") String accessToken,
            @Valid @RequestBody PasswordCheckRequest request) {

        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        Boolean isMatch = userService.prePasswordVerification(token, request.getPassword());

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.PRE_PASSWORD_VERIFICATION_SUCCESS, isMatch));
    }

    @Operation(summary = "비밀번호 변경", description = "유저의 비밀번호를 변경합니다.")
    @PatchMapping("/{loginId}/{email}")
    public ResponseEntity<GlobalResponse> changePassword(
            @RequestHeader("AccessToken") String accessToken,
            @Valid @RequestBody PasswordCheckRequest request) {

        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        Boolean isChange = userService.changePassword(token, request.getPassword());

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.CHANGE_PASSWORD_SUCCESS, isChange));
    }
}