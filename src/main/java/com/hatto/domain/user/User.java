package com.hatto.domain.user;

import com.hatto.common.BaseEntity;
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
    private boolean isDeleted;

    @Column
    private String profileImg;

    @Column
    private String fcmToken;

    @Builder
    public User(String email, String nickname, String loginId, String password) {
        this.email = email;
        this.nickname = nickname;
        this.loginId = loginId;
        this.password = password;
    }

    public boolean isDeleted() {
        return false;
    }
}
