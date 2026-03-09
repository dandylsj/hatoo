package com.hatto.domain.user;

import com.hatto.common.email.SmtpEmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final SmtpEmailSender smtpEmailSender;

    private final UserRepository userRepository;
    private final EmailRepository emailRepository;

    //이메일 중복체크 후 인증코드 전송
    @Transactional
    public boolean checkEmailSend(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            return false;
        } else {
            String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(7);

            // 기존 내역이 있으면 업데이트, 없으면 생성
            EmailVerification verification = emailRepository.findByEmail(email)
                    .orElse(new EmailVerification(email, code, expiry));

            if (verification.getId() != null) {
                verification.updateCode(code, expiry);
            }
            emailRepository.save(verification);

            smtpEmailSender.sendVerificationCode(email, code);
        }
        return true;
    }
}
