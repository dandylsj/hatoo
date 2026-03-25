package com.hatoo.domain.user;

import com.hatoo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String status;

    @Column
    private String loginId;

    @Column
    private String password;

    @Column
    private String email;

    @Column
    private String nickname;

    @Column
    private Boolean isDeleted = false;

    @Column
    private String profileImg;

    @Column
    private String fcmToken;

    @Column
    private Boolean isTermsAgreed = false;

    @Column
    private Boolean isPrivacyAgreed = false;

    @Column
    private Boolean isOverFourteen = false;

    @Column
    private Boolean isChoreNotiAllowed = false;

    @Column
    private Boolean isMarketingNotiAllowed = false;

    @Builder
    public User(String email, String nickname, String loginId, String password) {
        this.email = email;
        this.nickname = nickname;
        this.loginId = loginId;
        this.password = password;
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.isDeleted);
    }

    public void updateInfo(String nickname, String password, String profileImg, String fcmToken) {
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }
        if (password != null && !password.isBlank()) {
            this.password = password;
        }
        if (profileImg != null) {
            this.profileImg = profileImg;
        }
        if (fcmToken != null) {
            this.fcmToken = fcmToken;
        }
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
