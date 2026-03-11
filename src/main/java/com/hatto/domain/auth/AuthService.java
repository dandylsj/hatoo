package com.hatto.domain.auth;

import com.hatto.common.exception.CustomException;
import com.hatto.common.exception.ErrorMessage;
import com.hatto.common.util.JwtUtil;
import com.hatto.domain.auth.dto.LoginRequest;
import com.hatto.domain.auth.dto.SignRequest;
import com.hatto.domain.auth.dto.UserInfoResposne;
import com.hatto.domain.token.RefreshToken;
import com.hatto.domain.token.RefreshTokenRepository;
import com.hatto.domain.token.TokenResponse;
import com.hatto.domain.user.User;
import com.hatto.domain.user.UserRepository;
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


    //회원가입
    @Transactional
    public TokenResponse signup(SignRequest request) {

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // 활동 중인 유저면 가입불가
            if (!user.isDeleted()) {
                throw new CustomException(ErrorMessage.DUPLICATE_EMAIL);
            }

        }
        User user = new User(
                request.getEmail(),
                request.getNickname(),
                request.getLoginId(),
                passwordEncoder.encode(request.getPassword())


        );

        userRepository.save(user);

        //토큰생성
        String accessToken = jwtUtil.generateAccessToken(user.getLoginId(), user.getNickname());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // DB에 Refresh Token 저장 (기존 토큰 있으면 update)
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
    public UserInfoResposne getUserInfoApi(String loginId) {

        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

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
