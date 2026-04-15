package com.hatoo.domain.alarmUserAgree;

import com.hatoo.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "AlarmUserAgree")
@Getter
@NoArgsConstructor
public class AlarmUserAgree {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column
    private Boolean isChoreNotiAllowed;

    @Column
    private Boolean isMarketingNotiAllowed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    public AlarmUserAgree(Boolean isChoreNotiAllowed, Boolean isMarketingNotiAllowed, User user) {
        this.isChoreNotiAllowed = isChoreNotiAllowed;
        this.isMarketingNotiAllowed = isMarketingNotiAllowed;
        this.user = user;
    }
}
