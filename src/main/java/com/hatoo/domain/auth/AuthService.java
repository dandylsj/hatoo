package com.hatoo.domain.auth;

import com.hatoo.common.exception.CustomException;
import com.hatoo.common.exception.ErrorMessage;
import com.hatoo.common.util.JwtUtil;
import com.hatoo.domain.auth.dto.LoginRequest;
import com.hatoo.domain.auth.dto.SignRequest;
import com.hatoo.domain.auth.dto.UserInfoResposne;
import com.hatoo.domain.groupMember.GroupMember;
import com.hatoo.domain.groupMember.GroupMemberRepository;
import com.hatoo.domain.groupMember.ProfileImg;
import com.hatoo.domain.groups.Group;
import com.hatoo.domain.groups.GroupRepository;
import com.hatoo.domain.token.RefreshToken;
import com.hatoo.domain.token.RefreshTokenRepository;
import com.hatoo.domain.token.TokenResponse;
import com.hatoo.domain.user.User;
import com.hatoo.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    //회원가입
    @Transactional
    public TokenResponse signup(SignRequest request) {

        // 이메일 중복 확인
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException(ErrorMessage.DUPLICATE_EMAIL);
        }

        // 새 유저 생성
        User user = new User(
                request.getEmail(),
                request.getNickname(),
                request.getLoginId(),
                passwordEncoder.encode(request.getPassword())
        );
        userRepository.save(user);

        // 기본 그룹 생성 및 GroupMember 등록
        Group defaultGroup = new Group(
                request.getNickname(),
                "기본 그룹",
                user.getId()
        );
        groupRepository.save(defaultGroup);

        GroupMember defaultGroupMember = new GroupMember(user, defaultGroup, null);
        groupMemberRepository.save(defaultGroupMember);

        String accessToken = jwtUtil.generateAccessToken(user.getLoginId(), user.getNickname());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUserId(user.getId())
                .orElse(new RefreshToken(user.getId(), refreshToken));
        refreshTokenEntity.updateToken(refreshToken);
        refreshTokenRepository.save(refreshTokenEntity);

        return new TokenResponse(accessToken, refreshToken);
    }

    //로그인
    @Transactional
    public TokenResponse login(LoginRequest request) {

        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorMessage.INVALID_PASSWORD);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getLoginId(), user.getNickname());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUserId(user.getId())
                .orElse(new RefreshToken(user.getId(), refreshToken));
        refreshTokenEntity.updateToken(refreshToken);
        refreshTokenRepository.save(refreshTokenEntity);

        return new TokenResponse(accessToken, refreshToken);
    }

    //유저정보 불러오기
    @Transactional(readOnly = true)
    public UserInfoResposne getUserInfoApi(String accessToken) {

        jwtUtil.validateToken(accessToken);

        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        return new UserInfoResposne(
                user.getId(),
                user.getStatus(),
                user.getLoginId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImg(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
