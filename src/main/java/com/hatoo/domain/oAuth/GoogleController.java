package com.hatoo.domain.oAuth;

import com.hatoo.domain.token.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/google")
@Tag(name = "Auth", description = "인증(회원가입, 로그인 등) 관련 API")
public class GoogleController {

    private final GoogleService googleService;

    @Operation(summary = "구글 로그인", description = "구글에서 발급받은 idToken으로 로그인합니다. 신규 유저는 자동으로 회원가입됩니다.")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> googleLogin(@RequestBody GoogleLoginRequest request) {
        TokenResponse token = googleService.googleLogin(request.getIdToken());
        return ResponseEntity.ok(token);
    }
}
