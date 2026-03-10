package com.hatto.domain.user;

import com.hatto.common.model.enums.SuccessMessage;
import com.hatto.common.model.response.GlobalResponse;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class EmailController {

    private final EmailService emailService;

    //이메일 중복 확인 및 인증코드 발송
    @GetMapping("/check-email")
    public ResponseEntity<GlobalResponse> checkEmail(@RequestParam @Email String email) {

        boolean isSent = emailService.checkEmailSend(email);

        if (isSent) {
            return ResponseEntity.ok(GlobalResponse.successNodata(SuccessMessage.SEND_AUTHENTICATION_CODE));
        } else {
            return ResponseEntity.ok(GlobalResponse.successNodata(SuccessMessage.EMAIL_VERIFICATION_DUPLICATED));
        }
    }

    //이메일 코드 인증
    @PostMapping("/check-email")
    public ResponseEntity<GlobalResponse> enterTheVerificationCode(@RequestParam @Email String email,String token) {

         boolean verify = emailService.enterTheVerifcationCodeApi(email,token);

        return ResponseEntity.ok(GlobalResponse.success(SuccessMessage.EMAIL_VERIFICATION_SUCCESSFUL,verify));
    }
}
