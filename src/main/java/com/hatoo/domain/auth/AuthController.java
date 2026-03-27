package com.hatoo.domain.auth;

import com.hatoo.common.model.enums.SuccessMessage;
import com.hatoo.common.model.response.GlobalResponse;
import com.hatoo.domain.auth.dto.LoginRequest;
import com.hatoo.domain.auth.dto.SignRequest;
import com.hatoo.domain.auth.dto.UserInfoResposne;
import com.hatoo.domain.token.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증(회원가입, 로그인 등) 관련 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "새로운 유저를 등록합니다.")
    @PostMapping("/sign")
    public ResponseEntity sign(@Valid @RequestBody SignRequest request) {

        TokenResponse tokenResponse = authService.signup(request);

        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "로그인", description = "아이디와 비밀번호로 로그인하여 토큰을 발급받습니다.")
    @PostMapping("/login")
    public ResponseEntity login(@Valid @RequestBody LoginRequest request) {

        TokenResponse tokenResponse = authService.login(request);

        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "유저 프로필 조회", description = "로그인 아이디를 기반으로 유저의 프로필 정보를 조회합니다.")
    @GetMapping("/profile")
    public ResponseEntity userInfo(@RequestHeader("Authorization") String accessToken) {

        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        UserInfoResposne user = authService.getUserInfoApi(token);

        return ResponseEntity.ok(user);
    }
}
