package com.hatto.domain.auth;

import com.hatto.common.model.enums.SuccessMessage;
import com.hatto.common.model.response.GlobalResponse;
import com.hatto.domain.auth.dto.LoginRequest;
import com.hatto.domain.auth.dto.SignRequest;
import com.hatto.domain.auth.dto.UserInfoResposne;
import com.hatto.domain.token.TokenResponse;
import com.hatto.domain.user.User;
import com.hatto.domain.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //회원가입
    @PostMapping("/sign")
    public ResponseEntity<GlobalResponse> sign(@Valid @RequestBody SignRequest request) {

        TokenResponse tokenResponse = authService.signup(request);

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.AUTH_SIGNUP_SUCCESS, tokenResponse));
    }

    //로그인
    @PostMapping("/login")
    public ResponseEntity<GlobalResponse> login(@Valid @RequestBody LoginRequest request) {

        TokenResponse tokenResponse = authService.login(request);

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.AUTH_LOGIN_SUCCESS, tokenResponse));
    }

    //유저 정보 불러오기
    @GetMapping("/profile")
    public ResponseEntity<GlobalResponse> userInfo(@RequestParam String loginId) {

        UserInfoResposne user = authService.getUserInfoApi(loginId);

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.MY_READ_SUCCESS, user));
    }
}
