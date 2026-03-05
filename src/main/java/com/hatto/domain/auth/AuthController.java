package com.hatto.domain.auth;

import com.hatto.common.model.enums.SuccessMessage;
import com.hatto.common.model.response.GlobalResponse;
import com.hatto.domain.auth.dto.SignRequest;
import com.hatto.domain.token.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
