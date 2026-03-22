package com.hatto.domain.user;

import com.hatto.common.exception.CustomException;
import com.hatto.common.exception.ErrorMessage;
import com.hatto.common.util.JwtUtil;
import com.hatto.domain.user.dto.UserInfoModifyRequest;
import com.hatto.domain.user.dto.UserInfoModifyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public boolean checkLoginIdApi(String loginId) {

        if(userRepository.existsByLoginId(loginId)) {
            return true;
        }
        return false;
    }

    @Transactional
    public boolean checkNicknameApi(String nickname) {

        if(userRepository.existsByNickname(nickname)) {
            return true;
        }
        return false;
    }

    //유저 정보 수정
    @Transactional
    public UserInfoModifyResponse userInfoModifyResponse(String AccessToken, UserInfoModifyRequest request) {
        // 1. 토큰 검증
        jwtUtil.validateToken(AccessToken);

        // 2. 토큰에서 로그인 아이디 추출
        String loginId = jwtUtil.extractLoginId(AccessToken);

        // 3. 유저 조회
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        // 4. 비밀번호 암호화 (수정 요청에 비밀번호가 있는 경우)
        String encodedPassword = null;
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            encodedPassword = passwordEncoder.encode(request.getPassword());
        }

        // 5. 유저 정보 수정
        user.updateInfo(
                request.getNickname(),
                encodedPassword,
                request.getProfileImg(),
                request.getFcmToken()
        );

        // 6. 응답 DTO 생성 반환
        return new UserInfoModifyResponse(user.getId(), user.getNickname(), user.getProfileImg());
    }
}
