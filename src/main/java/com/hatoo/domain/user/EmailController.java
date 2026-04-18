package com.hatoo.domain.user;

import com.hatoo.common.model.response.GlobalResponse;
import com.hatoo.domain.user.dto.EmailVerifiRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Email", description = "이메일 인증 관련 API")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class EmailController {

    private final EmailService emailService;

    @Operation(summary = "이메일 중복 확인 및 인증코드 발송", description = "입력한 이메일의 중복 여부를 확인하고, 가입되지 않은 이메일이면 인증코드를 발송합니다.")
    @GetMapping("/check-email")
    public ResponseEntity<GlobalResponse<Boolean>> checkEmail(@RequestParam @Email String email) {
        boolean isSent = emailService.checkEmailSend(email);
        if (isSent) return ResponseEntity.ok(GlobalResponse.success(true));
        return ResponseEntity.ok(GlobalResponse.fail());
    }

    @Operation(summary = "이메일 코드 인증", description = "이메일로 발송된 인증코드를 확인하여 이메일을 인증합니다.")
    @PostMapping("/check-email")
    public ResponseEntity<GlobalResponse<Boolean>> enterTheVerificationCode(@RequestBody EmailVerifiRequest request) {
        boolean verify = emailService.enterTheVerifcationCodeApi(request);
        if (verify) return ResponseEntity.ok(GlobalResponse.success(true));
        return ResponseEntity.ok(GlobalResponse.fail());
    }
}
