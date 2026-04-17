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
import lombok.Getter;
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
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@Getter
@Slf4j
@RequiredArgsConstructor
public class NaverService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public TokenResponse naverLoginFromApp(String accessToken) {

        try {
            // 앱에서 받은 네이버 액세스 토큰으로 유저 정보 조회
            NaverUserInfo naverUserInfo = getNaverUserInfo(accessToken);

            // 신규 가입 또는 로그인 처리
            User user = registerOrLogin(naverUserInfo);

            // 우리 서비스 JWT 발급
            String ourAccessToken = jwtUtil.generateAccessToken(user.getLoginId(), user.getNickname());
            String ourRefreshToken = jwtUtil.generateRefreshToken(user.getId());

            // RefreshToken DB 저장 (있으면 갱신, 없으면 새로 생성)
            RefreshToken refreshTokenEntity = refreshTokenRepository.findByUserId(user.getId())
                    .orElse(new RefreshToken(user.getId(), ourRefreshToken));

            refreshTokenEntity.updateToken(ourRefreshToken);
            refreshTokenRepository.save(refreshTokenEntity);

            return new TokenResponse(ourAccessToken, ourRefreshToken);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("네이버 API 호출 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorMessage.NAVER_LOGIN_FAILED);
        }
    }

    // 앱에서 보내준 네이버 액세스 토큰으로 유저 정보를 조회하는 메서드
    private NaverUserInfo getNaverUserInfo(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

        ResponseEntity<NaverUserInfo> response = restTemplate.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.GET,
                request,
                NaverUserInfo.class
        );

        NaverUserInfo naverUserInfo = response.getBody();

        if (naverUserInfo == null || naverUserInfo.getResponse() == null) {
            throw new CustomException(ErrorMessage.NAVER_LOGIN_FAILED);
        }

        log.info("네이버 유저 정보 조회 성공: resultcode={}", naverUserInfo.getResultcode());
        return naverUserInfo;
    }

    // 기존 유저는 로그인, 새로운 유저는 자동 회원가입
    private User registerOrLogin(NaverUserInfo naverUserInfo) {

        NaverUserInfo.NaverResponse naverResponse = naverUserInfo.getResponse();

        String naverId = naverResponse.getId();
        String nickname = naverResponse.getNickname() != null
                ? naverResponse.getNickname()
                : (naverResponse.getName() != null ? naverResponse.getName() : "네이버유저");
        String naverEmail = naverResponse.getEmail();

        User user = userRepository.findByNaverId(naverId).orElse(null);

        if (user == null) {
            // 신규 네이버 유저 → 자동 회원가입
            String loginId = "naver_" + naverId;
            String email = (naverEmail != null && !naverEmail.isEmpty())
                    ? naverEmail
                    : "naver_" + naverId + "@hatoo.app";
            String password = UUID.randomUUID().toString();

            user = User.builder()
                    .email(email)
                    .nickname(nickname)
                    .loginId(loginId)
                    .password(passwordEncoder.encode(password))
                    .build();
            user.setNaverId(naverId);
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
