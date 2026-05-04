package com.hatoo.common.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpEmailSender {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendVerificationCode(String to, String code) {
        log.info("[Email] 인증코드 발송 시작 - to: {}", to);
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("[Hatoo] 회원가입 인증 코드 안내");

            Context context = new Context();
            context.setVariable("code", code);

            String htmlContent = templateEngine.process("mail", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("[Email] 인증코드 발송 성공 - to: {}", to);
        } catch (MessagingException e) {
            log.error("[Email] 인증코드 발송 실패 - to: {}, error: {}", to, e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("[Email] 인증코드 발송 중 예외 발생 - to: {}, error: {}", to, e.getMessage());
            throw e;
        }
    }
}
