package com.hatoo.domain.user;

import com.hatoo.common.model.response.GlobalResponse;
import com.hatoo.domain.user.dto.*;
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

    @Operation(summary = "필수 동의 저장", description = "소셜 로그인(네이버/카카오) 회원가입 직후 필수 동의 3개를 저장합니다.")
    @PostMapping("/agree")
    public ResponseEntity<GlobalResponse<Boolean>> saveUserAgree(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @RequestBody UserAgreeRequest request) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(GlobalResponse.success(userService.saveUserAgree(token, request)));
    }

    @Operation(summary = "전체 알림 설정 조회", description = "전체 알림/마케팅/개인/그룹 알림 설정과 그룹별 세부 설정을 한 번에 조회합니다.")
    @GetMapping("/alarm")
    public ResponseEntity<AlarmSettingResponse> getAlarmSetting(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(userService.getAlarmSetting(token));
    }

    @Operation(summary = "알림 설정 수정", description = "전체 알림/마케팅/개인/그룹 알림 설정을 수정합니다. null인 항목은 변경되지 않습니다. 응답으로 변경된 실제 설정값을 반환합니다.")
    @PatchMapping("/alarm")
    public ResponseEntity<AlarmSettingResponse> saveAlarmAgree(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @RequestBody AlarmAgreeRequest request) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(userService.saveAlarmAgree(token, request));
    }

    @Operation(summary = "아이디 중복 확인", description = "회원가입 시 로그인 아이디의 중복 여부를 확인합니다.")
    @GetMapping("/check-login-id")
    public ResponseEntity<Boolean> checkLoginId(@RequestParam String loginId) {
        return ResponseEntity.ok(userService.checkLoginIdApi(loginId));
    }

    @Operation(summary = "닉네임 중복 확인", description = "회원가입 시 닉네임의 중복 여부를 확인합니다.")
    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(userService.checkNicknameApi(nickname));
    }

    @Operation(summary = "유저 정보 수정", description = "로그인한 유저의 정보를 수정합니다.")
    @PatchMapping
    public ResponseEntity<UserInfoModifyResponse> modifyMemberInfo(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @RequestBody UserInfoModifyRequest userInfoModify) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(userService.userInfoModifyResponse(token, userInfoModify));
    }

    @Operation(summary = "이전 비밀번호 확인", description = "비밀번호 변경 전, 현재 비밀번호가 맞는지 확인합니다.")
    @PostMapping("/check-password")
    public ResponseEntity<Boolean> prePasswordVerification(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @Valid @RequestBody PasswordCheckRequest request) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(userService.prePasswordVerification(token, request));
    }

    @Operation(summary = "비밀번호 변경", description = "유저의 비밀번호를 변경합니다.")
    @PatchMapping("/{loginId}/{email}")
    public ResponseEntity<Boolean> changePassword(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @Valid @RequestBody PasswordCheckRequest request) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(userService.changePassword(token, request));
    }

    @Operation(summary = "회원탈퇴", description = "회원탈퇴 처리합니다. isDeleted가 true로 변경됩니다.")
    @DeleteMapping
    public ResponseEntity<Boolean> withdrawUser(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(userService.withdrawUser(token));
    }

    @Operation(summary = "아이디 찾기", description = "아이디를 찾습니다.")
    @GetMapping("/find-user-id")
    public ResponseEntity<Boolean> findUserId(@RequestParam String email) {

        return ResponseEntity.ok(userService.findUserIdApi(email));

    }

    @Operation(summary = "아이디 찾기 이메일 인증코드 입력", description = "이메일에 받은 코드를 적으면 아이디를 반환")
    @PostMapping("/email-code-find-id")
    public ResponseEntity<UserFindIdEmailCodeResponse> findIdEmailCode(@RequestBody EmailVerifiRequest request) {
        return ResponseEntity.ok(userService.enterTheVerifcationCodeApi(request));
    }

    @Operation(summary = "비밀번호 재설정 인증코드 발송", description = "아이디와 이메일이 일치하는 경우 인증코드를 발송합니다.")
    @PostMapping("/find-password/send-code")
    public ResponseEntity<Boolean> sendPasswordResetCode(@RequestBody PasswordResetSendRequest request) {
        return ResponseEntity.ok(userService.sendPasswordResetCode(request));
    }

    @Operation(summary = "비밀번호 재설정", description = "인증코드 확인 후 새 비밀번호로 변경합니다.")
    @PostMapping("/find-password/reset")
    public ResponseEntity<Boolean> resetPassword(@RequestBody PasswordResetConfirmRequest request) {
        return ResponseEntity.ok(userService.resetPassword(request));
    }
}
