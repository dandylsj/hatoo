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
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppleService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${apple.bundle-id:com.rfive.hatoo}")
    private String bundleId;

    /**
     * Apple 로그인 / 자동 회원가입
     *
     * @param identityToken iOS에서 받아온 Apple JWT 토큰
     * @param nickname      최초 로그인 시 iOS가 전달하는 사용자 이름 (이후 로그인에서는 null)
     */
    @Transactional
    public TokenResponse appleLogin(String identityToken, String nickname) {
        // 1단계: Apple identityToken 검증 → 유저 정보 추출
        AppleUserInfo appleUserInfo = verifyIdentityToken(identityToken);

        // 2단계: 신규 가입 or 기존 로그인 처리
        User user = registerOrLogin(appleUserInfo, nickname);

        // 3단계: 우리 서비스 JWT 발급
        String accessToken = jwtUtil.generateAccessToken(user.getLoginId(), user.getNickname());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 4단계: RefreshToken DB 저장 (있으면 갱신, 없으면 새로 생성)
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUserId(user.getId())
                .orElse(new RefreshToken(user.getId(), refreshToken));
        refreshTokenEntity.updateToken(refreshToken);
        refreshTokenRepository.save(refreshTokenEntity);

        return new TokenResponse(accessToken, refreshToken);
    }

    /**
     * Apple identityToken(JWT) 서명 검증 및 클레임 추출
     *
     * 흐름:
     * 1. JWT 파싱 → 헤더에서 kid(키 ID) 추출
     * 2. Apple 공개키 목록(JWKS) 조회: https://appleid.apple.com/auth/keys
     * 3. kid로 매칭되는 공개키 선택
     * 4. RSA 서명 검증
     * 5. 클레임(만료시각, iss, aud) 유효성 검사
     * 6. sub(Apple 고유 유저 ID), email 반환
     */
    private AppleUserInfo verifyIdentityToken(String identityToken) {
        try {
            // 1. JWT 파싱
            SignedJWT signedJWT = SignedJWT.parse(identityToken);

            // 2. JWT 헤더에서 kid 추출
            String kid = signedJWT.getHeader().getKeyID();

            // 3. Apple 공개키 목록 가져오기 (JWKS 엔드포인트)
            JWKSet jwkSet = JWKSet.load(new URL("https://appleid.apple.com/auth/keys"));
            JWK jwk = jwkSet.getKeyByKeyId(kid);
            if (jwk == null) {
                log.error("[Apple] 매칭되는 공개키 없음 - kid: {}", kid);
                throw new CustomException(ErrorMessage.INVALID_AUTH_INFO);
            }

            // 4. RSA 서명 검증
            RSAKey rsaKey = (RSAKey) jwk;
            JWSVerifier verifier = new RSASSAVerifier(rsaKey);
            if (!signedJWT.verify(verifier)) {
                log.error("[Apple] JWT 서명 검증 실패");
                throw new CustomException(ErrorMessage.INVALID_AUTH_INFO);
            }

            // 5. 클레임 추출 및 유효성 검사
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            // 만료 시간 확인
            if (claims.getExpirationTime() == null || claims.getExpirationTime().before(new Date())) {
                throw new CustomException(ErrorMessage.EXPIRED_TOKEN);
            }

            // 발급자 확인 (반드시 Apple이어야 함)
            if (!"https://appleid.apple.com".equals(claims.getIssuer())) {
                log.error("[Apple] 잘못된 발급자: {}", claims.getIssuer());
                throw new CustomException(ErrorMessage.INVALID_AUTH_INFO);
            }

            // audience 확인 (우리 앱의 Bundle ID와 일치해야 함)
            if (!claims.getAudience().contains(bundleId)) {
                log.error("[Apple] audience 불일치 - expected: {}, actual: {}", bundleId, claims.getAudience());
                throw new CustomException(ErrorMessage.INVALID_AUTH_INFO);
            }

            // 6. 유저 식별 정보 추출
            String sub = claims.getSubject();   // Apple 고유 유저 ID (변하지 않음)
            String email = claims.getStringClaim("email"); // null일 수 있음

            return new AppleUserInfo(sub, email);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Apple] identityToken 검증 중 오류: {}", e.getMessage());
            throw new CustomException(ErrorMessage.APPLE_LOGIN_FAILED);
        }
    }

    /**
     * 기존 유저면 로그인, 처음이면 자동 회원가입
     *
     * - nickname: iOS가 최초 로그인 시에만 전달. 두 번째 로그인부터는 null
     *   → null이면 "애플유저" 기본값 사용 (이미 DB에 이름이 있으니 덮어쓰지 않음)
     */
    private User registerOrLogin(AppleUserInfo appleUserInfo, String nickname) {
        String appleId = appleUserInfo.getSub();
        String appleEmail = appleUserInfo.getEmail();

        // 1. 이미 Apple로 로그인한 적 있는 유저 → 그냥 로그인
        User user = userRepository.findByAppleId(appleId).orElse(null);
        if (user != null) {
            return user;
        }

        // 2. 같은 이메일로 일반/다른 소셜 가입한 유저가 있으면 → 기존 계정에 Apple ID 연동
        if (appleEmail != null && !appleEmail.isEmpty()) {
            User existingUser = userRepository.findByEmail(appleEmail).orElse(null);
            if (existingUser != null) {
                log.info("[Apple] 기존 계정에 애플 연동 - email: {}, userId: {}", appleEmail, existingUser.getId());
                existingUser.setAppleId(appleId);
                return existingUser;
            }
        }

        // 3. 완전히 신규 유저 → 자동 회원가입
        // nickname이 null이면 기본값 사용 (두 번째 로그인 이후엔 Apple이 이름을 안 줌)
        String finalNickname = (nickname != null && !nickname.isBlank()) ? nickname : "애플유저";
        String loginId = "apple_" + appleId;
        String email = (appleEmail != null && !appleEmail.isEmpty())
                ? appleEmail
                : "apple_" + appleId + "@hatoo.app";
        String password = UUID.randomUUID().toString();

        user = User.builder()
                .email(email)
                .nickname(finalNickname)
                .loginId(loginId)
                .password(passwordEncoder.encode(password))
                .build();
        user.setAppleId(appleId);
        userRepository.save(user);

        // 기본 그룹 자동 생성
        Group defaultGroup = new Group(finalNickname, "기본 그룹", user.getId(), true);
        groupRepository.save(defaultGroup);
        GroupMember defaultGroupMember = new GroupMember(user, defaultGroup, user.getProfileImg(), true);
        groupMemberRepository.save(defaultGroupMember);

        log.info("[Apple] 신규 회원가입 완료 - loginId: {}, email: {}", loginId, email);
        return user;
    }
}
