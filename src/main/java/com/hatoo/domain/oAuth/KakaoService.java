package com.hatoo.domain.oAuth;

import com.hatoo.common.exception.CustomException;
import com.hatoo.common.exception.ErrorMessage;
import com.hatoo.common.util.JwtUtil;
import com.hatoo.domain.groupMember.GroupMember;
import com.hatoo.domain.groupMember.GroupMemberRepository;
import com.hatoo.domain.groups.Group;
import com.hatoo.domain.groups.GroupRepository;
import com.hatoo.domain.token.RefreshToken;
import com.hatoo.domain.token.RefreshTokenRepository;
import com.hatoo.domain.token.TokenResponse;
import com.hatoo.domain.user.User;
import com.hatoo.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate = new RestTemplate();

//    @Value("${kakao.client-id}")
//    private String clientId;

    // 앱용 로그인 (네이티브 SDK - redirect_uri 없이 토큰 교환)
    @Transactional
    public TokenResponse kakaoLoginFromApp(String code) {
        try {
            // 1단계: 인가 코드 → 카카오 액세스 토큰 교환
//            String kakaoAccessToken = getKakaoAccessToken(code);

            // 2단계: 카카오 액세스 토큰 → 유저 정보 조회
            KakaoUserInfo kakaoUserInfo = getKakaoUserInfo(code);

            // 3단계: 신규 가입 or 기존 로그인 처리
            User user = registerOrLogin(kakaoUserInfo);

            // 4단계: 우리 서비스 JWT 발급
            String accessToken = jwtUtil.generateAccessToken(user.getLoginId(), user.getNickname());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());

            // 5단계: RefreshToken DB 저장 (있으면 갱신, 없으면 새로 생성)
            RefreshToken refreshTokenEntity = refreshTokenRepository.findByUserId(user.getId())
                    .orElse(new RefreshToken(user.getId(), refreshToken));
            

            refreshTokenEntity.updateToken(refreshToken);
            refreshTokenRepository.save(refreshTokenEntity);

            return new TokenResponse(accessToken, refreshToken);

        } catch (RestClientException e) {
            log.error("카카오 API 호출 실패: {}", e.getMessage());
            throw new CustomException(ErrorMessage.KAKAO_LOGIN_FAILED);
        }
    }

    // 카카오에 인가 코드를 보내고 액세스 토큰을 받아오는 메서드 (redirect_uri 없음)
//    private String getKakaoAccessToken(String code) {
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
//
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//        body.add("grant_type", "authorization_code");
//        body.add("client_id", clientId);
//        body.add("code", code);
//
//        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
//
//        ResponseEntity<KakaoTokenResponse> response = restTemplate.exchange(
//                "https://kauth.kakao.com/oauth/token",
//                HttpMethod.POST,
//                request,
//                KakaoTokenResponse.class
//        );
//
//        if (response.getBody() == null || response.getBody().getAccessToken() == null) {
//            throw new CustomException(ErrorMessage.INVALID_AUTH_INFO);
//        }
//
//        return response.getBody().getAccessToken();
//    }

    // 카카오 액세스 토큰으로 유저 정보를 조회하는 메서드
    private KakaoUserInfo getKakaoUserInfo(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

        ResponseEntity<KakaoUserInfo> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                request,
                KakaoUserInfo.class
        );

        if (response.getBody() == null) {
            throw new CustomException(ErrorMessage.USER_NOT_FOUND);
        }

        return response.getBody();
    }

    // 기존 유저면 로그인, 처음이면 자동 회원가입하는 메서드
    private User registerOrLogin(KakaoUserInfo kakaoUserInfo) {

        Long kakaoId = kakaoUserInfo.getId();
        String nickname = kakaoUserInfo.getProperties().getNickname();
        
        // 카카오 계정의 이메일 가져오기
        String kakaoEmail = null;
        if (kakaoUserInfo.getKakaoAccount() != null) {
            kakaoEmail = kakaoUserInfo.getKakaoAccount().getEmail();
        }

        User user = userRepository.findByKakaoId(kakaoId).orElse(null);

        if (user == null) {
            // 신규 카카오 유저 → 자동 회원가입
            String loginId = "kakao_" + kakaoId;
            // 실제 이메일이 제공되지 않으면 임시 이메일 사용
            String email = (kakaoEmail != null && !kakaoEmail.isEmpty()) ? kakaoEmail : "kakao_" + kakaoId + "@hatoo.app";
            String password = UUID.randomUUID().toString();

            user = User.builder()
                    .email(email)
                    .nickname(nickname)
                    .loginId(loginId)
                    .password(passwordEncoder.encode(password))
                    .build();
            user.setKakaoId(kakaoId);
            userRepository.save(user);

            // 기본 그룹 자동 생성
            Group defaultGroup = new Group(nickname, "기본 그룹", user.getId(), true);
            groupRepository.save(defaultGroup);
            GroupMember defaultGroupMember = new GroupMember(user, defaultGroup, user.getProfileImg(), true);
            groupMemberRepository.save(defaultGroupMember);
        }

        return user;
    }
}
