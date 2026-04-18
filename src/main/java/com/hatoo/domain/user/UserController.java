package com.hatoo.domain.user;

import com.hatoo.common.model.response.GlobalResponse;
import com.hatoo.domain.user.dto.UserInfoModifyRequest;
import com.hatoo.domain.user.dto.UserInfoModifyResponse;
import com.hatoo.domain.user.dto.PasswordCheckRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    public ResponseEntity<GlobalResponse<Boolean>> checkLoginId(@RequestParam String loginId) {
        boolean checked = userService.checkLoginIdApi(loginId);
        if (!checked) return ResponseEntity.ok(GlobalResponse.fail());
        return ResponseEntity.ok(GlobalResponse.success(true));
    }

    @Operation(summary = "닉네임 중복 확인", description = "회원가입 시 닉네임의 중복 여부를 확인합니다.")
    @GetMapping("/check-nickname")
    public ResponseEntity<GlobalResponse<Boolean>> checkNickname(@RequestParam String nickname) {
        boolean checkNickName = userService.checkNicknameApi(nickname);
        if (!checkNickName) return ResponseEntity.ok(GlobalResponse.fail());
        return ResponseEntity.ok(GlobalResponse.success(true));
    }

    @Operation(summary = "유저 정보 수정", description = "로그인한 유저의 정보를 수정합니다.")
    @PatchMapping
    public ResponseEntity<GlobalResponse<UserInfoModifyResponse>> modifyMemberInfo(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @RequestBody UserInfoModifyRequest userInfoModify) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        UserInfoModifyResponse response = userService.userInfoModifyResponse(token, userInfoModify);
        return ResponseEntity.ok(GlobalResponse.success(response));
    }

    @Operation(summary = "이전 비밀번호 확인", description = "비밀번호 변경 전, 현재 비밀번호가 맞는지 확인합니다.")
    @GetMapping("/check-password")
    public ResponseEntity<GlobalResponse<Boolean>> prePasswordVerification(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @Valid @RequestBody PasswordCheckRequest request) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        boolean isMatch = userService.prePasswordVerification(token, request.getPassword());
        if (!isMatch) return ResponseEntity.ok(GlobalResponse.fail());
        return ResponseEntity.ok(GlobalResponse.success(true));
    }

    @Operation(summary = "비밀번호 변경", description = "유저의 비밀번호를 변경합니다.")
    @PatchMapping("/{loginId}/{email}")
    public ResponseEntity<GlobalResponse<Boolean>> changePassword(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @Valid @RequestBody PasswordCheckRequest request) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        boolean isChange = userService.changePassword(token, request.getPassword());
        if (!isChange) return ResponseEntity.ok(GlobalResponse.fail());
        return ResponseEntity.ok(GlobalResponse.success(true));
    }

    @Operation(summary = "회원탈퇴", description = "회원탈퇴 처리합니다. isDeleted가 true로 변경됩니다.")
    @DeleteMapping
    public ResponseEntity<GlobalResponse<Boolean>> withdrawUser(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        userService.withdrawUser(token);
        return ResponseEntity.ok(GlobalResponse.success(true));
    }
}

 