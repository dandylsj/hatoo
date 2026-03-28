package com.hatoo.domain.user;

import com.hatoo.common.BaseEntity;
import com.hatoo.domain.groups.Group;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column
    private String status;

    @Column(unique = true)
    private String loginId;

    @Column
    private String password;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    private Group group;

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

    /**
     * 유저를 특정 그룹에 소속시킵니다.
     * 연관관계의 주인인 User 엔티티의 group 필드를 설정합니다.
     *
     * @return
     */
    public boolean assignGroup(Group group) {
        this.group = group;
        return true;
    }
}
