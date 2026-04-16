package com.hatoo.domain.kakao;

import com.hatoo.domain.token.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/kakao")
@Tag(name = "Auth", description = "인증(회원가입, 로그인 등) 관련 API")
public class KakaoController {

    private final KakaoService kakaoService;

//    @Value("${KAKAO_APP_REDIRECT_URI}")
//    private String frontendUrl;

    // 웹용: 카카오가 인증 후 보내주는 인가 코드를 받는 콜백 엔드포인트
//    @PostMapping("/web/login")
//    public void kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
//
//        TokenResponse token = kakaoService.kakaoLogin(code);
//
//        // 슬래시(/) 중복 방지
//        String base = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
//
//        // 토큰을 URL에 인코딩해서 프론트엔드로 리다이렉트
//        String accessToken = URLEncoder.encode(token.getAccessToken(), StandardCharsets.UTF_8);
//        String refreshToken = URLEncoder.encode(token.getRefreshToken(), StandardCharsets.UTF_8);
//
//        String redirectUrl = base + "/oauth/kakao/callback"
//                + "?accessToken=" + accessToken
//                + "&refreshToken=" + refreshToken;
//
//        response.sendRedirect(redirectUrl);
//    }

    // 앱용: 모바일 앱이 카카오에서 받은 인가 코드를 백엔드로 직접 전달
    @Operation(summary = "카카오 로그인", description = "카카오 앱을 통해 로그인")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> kakaoAppLogin(@RequestBody KakaoLoginRequest request) {
        TokenResponse token = kakaoService.kakaoLoginFromApp(request.getCode());
        return ResponseEntity.ok(token);
    }
}
