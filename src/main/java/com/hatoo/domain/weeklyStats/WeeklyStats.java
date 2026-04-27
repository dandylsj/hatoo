package com.hatoo.domain.weeklyStats;

import com.hatoo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "weekly_stats")
@Getter
@NoArgsConstructor
public class WeeklyStats extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, columnDefinition = "BINARY(16)")
    private UUID groupId;

    @Column(nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = true)
    private String profileImg;

    @Column(nullable = false)
    private String weekStart;   // 이번 주 월요일 (yyyy-MM-dd)

    @Column(nullable = false)
    private String weekEnd;     // 이번 주 일요일 (yyyy-MM-dd)

    @Column(nullable = false)
    private int totalCount;     // 이번 주 총 할일 수

    @Column(nullable = false)
    private int finishedCount;  // 이번 주 완료 할일 수

    @Column(nullable = false)
    private int percent;        // 완료율

    public WeeklyStats(UUID groupId, UUID userId, String nickname, String profileImg,
                       String weekStart, String weekEnd,
                       int totalCount, int finishedCount, int percent) {
        this.groupId = groupId;
        this.userId = userId;
        this.nickname = nickname;
        this.profileImg = profileImg;
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.totalCount = totalCount;
        this.finishedCount = finishedCount;
        this.percent = percent;
    }
}
