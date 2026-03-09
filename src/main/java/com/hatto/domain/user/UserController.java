package com.hatto.domain.user;

import com.hatto.common.model.enums.SuccessMessage;
import com.hatto.common.model.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;


    @GetMapping("/check-login-id")
    public ResponseEntity<GlobalResponse> checkLoginId(String loginId) {

        Boolean checked = userService.checkLoginIdApi(loginId);

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.USER_CHECK_LOGIN_ID_SUCCESS, checked));
    }

}
