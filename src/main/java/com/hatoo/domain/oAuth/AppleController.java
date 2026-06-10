package com.hatoo.domain.oAuth;

import com.hatoo.domain.token.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/apple")
@Tag(name = "Auth", description = "인증(회원가입, 로그인 등) 관련 API")
public class AppleController {

    private final AppleService appleService;

    @Operation(
        summary = "애플 로그인",
        description = "iOS 앱에서 Apple 로그인 후 받은 identityToken으로 로그인/자동가입 처리. " +
                      "최초 로그인 시에만 nickname을 함께 전달해주세요."
    )
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> appleLogin(@RequestBody AppleLoginRequest request) {
        TokenResponse token = appleService.appleLogin(
                request.getIdentityToken(),
                request.getNickname()
        );
        return ResponseEntity.ok(token);
    }
}
