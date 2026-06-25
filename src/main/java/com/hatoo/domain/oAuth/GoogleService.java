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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GOOGLE_TOKEN_INFO_URL =
            "https://oauth2.googleapis.com/tokeninfo?id_token=";

    @Transactional
    public TokenResponse googleLogin(String idToken) {
        try {
            // 1단계: idToken → Google tokeninfo로 검증 및 유저 정보 추출
            GoogleTokenInfo tokenInfo = verifyIdToken(idToken);

            // 2단계: 신규 가입 or 기존 로그인 처리
            User user = registerOrLogin(tokenInfo);

            // 3단계: 우리 서비스 JWT 발급
            String accessToken = jwtUtil.generateAccessToken(user.getLoginId(), user.getNickname());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());

            // 4단계: RefreshToken DB 저장 (있으면 갱신, 없으면 새로 생성)
            RefreshToken refreshTokenEntity = refreshTokenRepository.findByUserId(user.getId())
                    .orElse(new RefreshToken(user.getId(), refreshToken));
            refreshTokenEntity.updateToken(refreshToken);
            refreshTokenRepository.save(refreshTokenEntity);

            return new TokenResponse(accessToken, refreshToken);

        } catch (RestClientException e) {
            log.error("[Google] idToken 검증 실패: {}", e.getMessage());
            throw new CustomException(ErrorMessage.GOOGLE_LOGIN_FAILED);
        }
    }

    // Google tokeninfo 엔드포인트로 idToken 검증
    private GoogleTokenInfo verifyIdToken(String idToken) {
        GoogleTokenInfo tokenInfo = restTemplate.getForObject(
                GOOGLE_TOKEN_INFO_URL + idToken,
                GoogleTokenInfo.class
        );

        if (tokenInfo == null || tokenInfo.getSub() == null) {
            throw new CustomException(ErrorMessage.GOOGLE_LOGIN_FAILED);
        }

        return tokenInfo;
    }

    // 기존 유저면 로그인, 처음이면 자동 회원가입
    private User registerOrLogin(GoogleTokenInfo tokenInfo) {
        String googleId = tokenInfo.getSub();
        String email = tokenInfo.getEmail();
        String nickname = tokenInfo.getName() != null ? tokenInfo.getName() : "구글유저";

        // 1. 이미 구글로 로그인한 적 있는 유저 → 그냥 로그인
        User user = userRepository.findByGoogleId(googleId).orElse(null);
        if (user != null) {
            return user;
        }

        // 2. 같은 이메일로 다른 방식으로 가입한 유저가 있으면 → 로그인 차단
        if (email != null && !email.isEmpty()) {
            if (userRepository.findByEmail(email).isPresent()) {
                log.warn("[Google] 다른 소셜 계정으로 이미 가입된 이메일 - email: {}", email);
                throw new CustomException(ErrorMessage.SOCIAL_LOGIN_ACCOUNT);
            }
        }

        // 3. 완전히 신규 유저 → 자동 회원가입
        String loginId = "google_" + googleId;
        String userEmail = (email != null && !email.isEmpty())
                ? email
                : "google_" + googleId + "@hatoo.app";
        String password = UUID.randomUUID().toString();

        user = User.builder()
                .email(userEmail)
                .nickname(nickname)
                .loginId(loginId)
                .password(passwordEncoder.encode(password))
                .build();
        user.setGoogleId(googleId);
        userRepository.save(user);

        // 기본 그룹 자동 생성
        Group defaultGroup = new Group(nickname, "기본 그룹", user.getId(), true);
        groupRepository.save(defaultGroup);
        GroupMember defaultGroupMember = new GroupMember(user, defaultGroup, user.getProfileImg(), true);
        groupMemberRepository.save(defaultGroupMember);

        log.info("[Google] 신규 회원가입 완료 - loginId: {}, email: {}", loginId, userEmail);
        return user;
    }
}
