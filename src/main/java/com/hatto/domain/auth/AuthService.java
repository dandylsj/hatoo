package com.hatto.domain.auth;

import com.hatto.common.exception.CustomException;
import com.hatto.common.exception.ErrorMessage;
import com.hatto.domain.auth.dto.SignRequest;
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
    public void signup(SignRequest request) {

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

        String accessToken = jwtUtil.generateToken(user.getId(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getRole());

        // DB에 Refresh Token 저장 (기존 토큰 있으면 update)
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUserId(user.getId())
                .orElse(new RefreshToken(user.getId(), refreshToken));

        refreshTokenEntity.updateToken(refreshToken);
        refreshTokenRepository.save(refreshTokenEntity);
    }
}
