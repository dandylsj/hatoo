package com.hatto.domain.user;

import com.hatto.common.email.SmtpEmailSender;
import com.hatto.common.exception.CustomException;
import com.hatto.common.exception.ErrorMessage;
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
            String token = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(7);

            // 기존 내역이 있으면 업데이트, 없으면 생성
            EmailVerification verification = emailRepository.findByEmail(email)
                    .orElse(new EmailVerification(email, token, expiry));

            if (verification.getId() != null) {
                verification.updateCode(token, expiry);
            }
            emailRepository.save(verification);

            smtpEmailSender.sendVerificationCode(email, token);
        }
        return true;
    }

    //이메일 코드 인증
    @Transactional
    public boolean enterTheVerifcationCodeApi(String email,String token) {

        EmailVerification verification = emailRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorMessage.EMAIL_NOT_FOUND));

        if (verification.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorMessage.INVALID_TIME_VERIFICATION_CODE); // 만료됨
        }
        if (!verification.getToken().equals(token)) {
            throw new CustomException(ErrorMessage.INVALID_VERIFICATION_CODE);
        }

        return true;
    }

}
