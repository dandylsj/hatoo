package com.hatoo.domain.auth;

import com.hatoo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    // 전송 횟수 제한 및 쿨타임용 필드
    private LocalDateTime lastSendTime;

    @Column(nullable = false)
    private int sendCount = 0;

    private LocalDateTime countResetTime;

    public EmailVerification(String email, String token, LocalDateTime expiryDate) {
        this.email = email;
        this.token = token;
        this.expiryDate = expiryDate;
    }

    public void updateCode(String token, LocalDateTime expiryDate) {
        this.token = token;
        this.expiryDate = expiryDate;
    }

    public void resetCount(LocalDateTime now, int countResetMinutes) {
        this.sendCount = 0;
        this.countResetTime = now.plusMinutes(countResetMinutes);
    }

    public void recordSend(LocalDateTime now) {
        this.lastSendTime = now;
        this.sendCount++;
    }
}
