package com.hatoo.domain.oAuth;

import com.hatoo.common.model.response.GlobalResponse;
import com.hatoo.domain.token.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/naver")
@Tag(name = "Auth", description = "인증(회원가입, 로그인 등) 관련 API")
public class NaverController {

    private final NaverService naverService;

    @Operation(summary = "네이버 로그인", description = "네이버 앱을 통해 로그인")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> naverAppLogin(@RequestBody NaverLoginRequest request) {
        TokenResponse token = naverService.naverLoginFromApp(request.getCode());
        return ResponseEntity.ok(token);
    }
}
