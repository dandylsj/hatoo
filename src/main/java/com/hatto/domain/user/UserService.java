package com.hatto.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;


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


}
