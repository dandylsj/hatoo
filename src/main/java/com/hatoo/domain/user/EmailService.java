package com.hatoo.domain.user;

import com.hatoo.common.email.SmtpEmailSender;
import com.hatoo.common.exception.CustomException;
import com.hatoo.common.exception.ErrorMessage;
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

    private static final int MAX_SEND_COUNT = 3;
    private static final int COOLDOWN_SECONDS = 10;
    private static final int COUNT_RESET_MINUTES = 5;

    //이메일 중복체크 후 인증코드 전송
    @Transactional
    public boolean checkEmailSend(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        EmailVerification verification = emailRepository.findByEmail(email)
                .orElse(new EmailVerification(email, "", now)); // New verification

        // 쿨다운 확인
        if (verification.getLastSendTime() != null && verification.getLastSendTime().plusSeconds(COOLDOWN_SECONDS).isAfter(now)) {
            throw new CustomException(ErrorMessage.EMAIL_SEND_COOLDOWN);
        }

        // 5분 내 전송 횟수 초기화 확인
        if (verification.getCountResetTime() != null && verification.getCountResetTime().isBefore(now)) {
            verification.resetCount(now, COUNT_RESET_MINUTES);
        }

        if (verification.getSendCount() >= MAX_SEND_COUNT) {
            throw new CustomException(ErrorMessage.EMAIL_SEND_LIMIT_EXCEEDED);
        }

        if (verification.getCountResetTime() == null) {
            verification.resetCount(now, COUNT_RESET_MINUTES);
        }

        String token = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        LocalDateTime expiry = now.plusMinutes(7);

        verification.updateCode(token, expiry);
        verification.recordSend(now);
        emailRepository.save(verification);

        smtpEmailSender.sendVerificationCode(email, token);

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
