package com.hatoo.domain.alarmUserAgree;

import com.hatoo.common.BaseEntity;
import com.hatoo.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "AlarmUserAgree")
@Getter
@NoArgsConstructor
public class AlarmUserAgree extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    // 전체 알림 마스터 토글 (OFF 시 모든 알림 차단)
    @Column
    private Boolean isAllNotiEnabled = true;

    // 마케팅 알림 수신 동의
    @Column
    private Boolean isMarketingNotiAllowed;

    // 개인 알림 (집안일 시작/마감 임박/마감 초과/배정)
    @Column
    private Boolean isPersonalNotiEnabled = true;

    // 그룹 알림 전체 마스터 (OFF 시 모든 그룹 알림 차단)
    @Column
    private Boolean isGroupNotiAllGlobalEnabled = true;

    // 기존 동의 필드 (onboarding 시 저장, 하위 호환 유지)
    @Column
    private Boolean isChoreNotiAllowed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public AlarmUserAgree(Boolean isChoreNotiAllowed, Boolean isMarketingNotiAllowed, User user) {
        this.isAllNotiEnabled = true;
        this.isChoreNotiAllowed = isChoreNotiAllowed;
        this.isMarketingNotiAllowed = isMarketingNotiAllowed;
        this.isPersonalNotiEnabled = true;
        this.isGroupNotiAllGlobalEnabled = true;
        this.user = user;
    }

    public void update(Boolean isAllNotiEnabled, Boolean isMarketingNotiAllowed,
                       Boolean isPersonalNotiEnabled, Boolean isGroupNotiAllGlobalEnabled) {
        if (isAllNotiEnabled != null) this.isAllNotiEnabled = isAllNotiEnabled;
        if (isMarketingNotiAllowed != null) this.isMarketingNotiAllowed = isMarketingNotiAllowed;
        if (isPersonalNotiEnabled != null) this.isPersonalNotiEnabled = isPersonalNotiEnabled;
        if (isGroupNotiAllGlobalEnabled != null) this.isGroupNotiAllGlobalEnabled = isGroupNotiAllGlobalEnabled;
    }
}
