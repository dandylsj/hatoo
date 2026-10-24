package com.hatoo.domain.activitylog;

import com.hatoo.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

// 유저 탈퇴 등으로 users 로우가 사라져도 가입 이력은 그대로 남도록 별도 테이블로 분리
@Entity
@Table(name = "user_signup_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSignupLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(columnDefinition = "BINARY(16)")
    private UUID userId;

    @Column
    private String email;

    @Column
    private String nickname;

    @Enumerated(EnumType.STRING)
    private SignupProvider provider;

    public UserSignupLog(UUID userId, String email, String nickname, SignupProvider provider) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.provider = provider;
    }
}
