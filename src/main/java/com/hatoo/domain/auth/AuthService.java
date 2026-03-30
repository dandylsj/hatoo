package com.hatoo.domain.auth;

import com.hatoo.common.exception.CustomException;
import com.hatoo.common.exception.ErrorMessage;
import com.hatoo.common.util.JwtUtil;
import com.hatoo.domain.auth.dto.LoginRequest;
import com.hatoo.domain.auth.dto.SignRequest;
import com.hatoo.domain.auth.dto.UserInfoResposne;
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

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final GroupRepository groupRepository;

    //회원가입
    @Transactional
    public TokenResponse signup(SignRequest request) {
        try {
            Optional<User> existingUser = userRepository.findByEmail(request.getEmail());

            if (existingUser.isPresent()) {
                User user = existingUser.get();
                if (!user.isDeleted()) {
                    return null; // 중복 이메일 시 null 반환 (실패)
                }
            }

            User user = new User(
                    request.getEmail(),
                    request.getNickname(),
                    request.getLoginId(),
                    passwordEncoder.encode(request.getPassword())
            );
            userRepository.save(user);

            // 회원가입 시 본인이 방장인 기본 그룹 자동 생성
            Group defaultGroup = new Group(
                    request.getNickname(),
                    "기본 그룹",
                    user.getId().toString()
            );
            groupRepository.save(defaultGroup);
            user.assignGroup(defaultGroup);
            userRepository.save(user);

            String accessToken = jwtUtil.generateAccessToken(user.getLoginId(), user.getNickname());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());

            RefreshToken refreshTokenEntity = refreshTokenRepository.findByUserId(user.getId())
                    .orElse(new RefreshToken(user.getId(), refreshToken));
            refreshTokenEntity.updateToken(refreshToken);
            refreshTokenRepository.save(refreshTokenEntity);

            return new TokenResponse(accessToken, refreshToken);
        } catch (Exception e) {
            return null;
        }
    }

    //로그인
    @Transactional
    public TokenResponse login(LoginRequest request) {
        try {
            User user = userRepository.findByLoginId(request.getLoginId())
                    .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return null;
            }

            String accessToken = jwtUtil.generateAccessToken(user.getLoginId(), user.getNickname());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());

            RefreshToken refreshTokenEntity = refreshTokenRepository.findByUserId(user.getId())
                    .orElse(new RefreshToken(user.getId(), refreshToken));
            refreshTokenEntity.updateToken(refreshToken);
            refreshTokenRepository.save(refreshTokenEntity);

            return new TokenResponse(accessToken, refreshToken);
        } catch (Exception e) {
            return null;
        }
    }

    //유저정보 불러오기
    @Transactional(readOnly = true)
    public UserInfoResposne getUserInfoApi(String accessToken) {
        try {
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
        } catch (Exception e) {
            return null;
        }
    }
}