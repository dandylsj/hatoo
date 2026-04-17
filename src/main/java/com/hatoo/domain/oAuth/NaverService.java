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
    public TokenResponse naverLoginFromApp(String code) {

        try{
            //프론트 앱에서 보내주는 토큰을 받아 옴.
            NaverUserInfo naverUserInfo = getNaverUserInfo(code);

            //신규 가입, 로그인 처리
            User user = registerOrLogin(naverUserInfo);

            // 우리 서비스 JWT 발급
            String accessToken = jwtUtil.generateAccessToken(user.getLoginId(), user.getNickname());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());

            // RefreshToken DB 저장 (있으면 갱신, 없으면 새로 생성)
            RefreshToken refreshTokenEntity = refreshTokenRepository.findByUserId(user.getId())
                    .orElse(new RefreshToken(user.getId(), refreshToken));

            refreshTokenEntity.updateToken(refreshToken);
            refreshTokenRepository.save(refreshTokenEntity);

            return new TokenResponse(accessToken, refreshToken);

        } catch (Exception e) {
            log.error("네이버 API 호출 실패: {}", e.getMessage());
            throw new CustomException(ErrorMessage.NAVER_LOGIN_FAILED);
        }
    }

    // 프론트에서 보내준 토큰을 네이버 서버에 요청하여 유저 정보를 받아오는 메서드
    private NaverUserInfo getNaverUserInfo(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

        ResponseEntity<NaverUserInfo> response = restTemplate.exchange(
                "https://nid.naver.com/oauth2.0/authorize",
                HttpMethod.GET,
                request,
                NaverUserInfo.class
        );

        if (response.getBody() == null) {
            throw new CustomException(ErrorMessage.USER_NOT_FOUND);
        }

        return response.getBody();
    }

    // 기존 유저는 로그인 , 새로운 회원은 회원가입
    private User registerOrLogin(NaverUserInfo naverUserInfo) {

        Long naverId = naverUserInfo.getId();
        String nickname = naverUserInfo.getProperties().getNickname();

        // 네이버 계정의 이메일 가져오기
        String naverEmail = null;
        if(naverUserInfo.getNaverAccount() != null) {
            naverEmail = naverUserInfo.getNaverAccount().getEmail();
        }

        User user = userRepository.findByNaverId(naverId).orElse(null);

        if(user == null) {
            // 신규 네이버 유저 → 자동 회원가입
            String loginId = "naver_" + naverId;
            // 실제 이메일이 제공되지 않으면 임시 이메일 사용
            String email = (naverEmail != null && !naverEmail.isEmpty()) ? naverEmail : "naver_" + naverId + "@hatoo.app";
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
