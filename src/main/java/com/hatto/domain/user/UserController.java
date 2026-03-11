package com.hatto.domain.user;

import com.hatto.common.model.enums.SuccessMessage;
import com.hatto.common.model.response.GlobalResponse;
import com.hatto.common.model.enums.SuccessMessage;
import com.hatto.common.model.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //아이디 중복 확인
    @GetMapping("/check-login-id")
    public ResponseEntity<GlobalResponse> checkLoginId(@RequestParam String loginId) {

        Boolean checked = userService.checkLoginIdApi(loginId);

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.USER_CHECK_LOGIN_ID_SUCCESS, checked));
    }

    //닉네임 중복 확인
    @GetMapping("/check-nickname")
    public ResponseEntity<GlobalResponse> checkNickname(@RequestParam String nickname) {

        Boolean checkNickName = userService.checkNicknameApi(nickname);

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.USER_CHECK_LOGIN_ID_SUCCESS, checkNickName));
    }
}
