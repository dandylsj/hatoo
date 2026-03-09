package com.hatto.domain.user;

import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class EmailController {

    private final EmailService emailService;

    //이메일 중복 확인 및 인증코드 발송
    @PostMapping("/check-email")
    public ResponseEntity<String> checkEmail(@RequestParam @Email String email) {
        boolean isSent = emailService.checkEmailSend(email);
        if (isSent) {
            return ResponseEntity.ok("인증 코드가 발송되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("이미 사용 중인 이메일입니다.");
        }
    }
}
