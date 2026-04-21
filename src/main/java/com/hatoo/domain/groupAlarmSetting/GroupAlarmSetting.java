package com.hatoo.domain.groupAlarmSetting;

import com.hatoo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "group_alarm_settings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "group_id"}))
@Getter
@NoArgsConstructor
public class GroupAlarmSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @Column(name = "group_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID groupId;

    // 그룹 알림 마스터 토글
    @Column(nullable = false)
    private Boolean isGroupNotiEnabled = true;

    // 새 집안일 등록 알림
    @Column(nullable = false)
    private Boolean isNewTaskNotiEnabled = true;

    // 새 멤버 추가 알림
    @Column(nullable = false)
    private Boolean isNewMemberNotiEnabled = true;

    // 집안일 완료 알림
    @Column(nullable = false)
    private Boolean isTaskCompleteNotiEnabled = true;

    public GroupAlarmSetting(UUID userId, UUID groupId) {
        this.userId = userId;
        this.groupId = groupId;
        this.isGroupNotiEnabled = true;
        this.isNewTaskNotiEnabled = true;
        this.isNewMemberNotiEnabled = true;
        this.isTaskCompleteNotiEnabled = true;
    }

    public void update(Boolean isGroupNotiEnabled, Boolean isNewTaskNotiEnabled,
                       Boolean isNewMemberNotiEnabled, Boolean isTaskCompleteNotiEnabled) {
        if (isGroupNotiEnabled != null) this.isGroupNotiEnabled = isGroupNotiEnabled;
        // 마스터 토글이 OFF 되면 세부 설정도 모두 OFF
        if (Boolean.FALSE.equals(isGroupNotiEnabled)) {
            this.isNewTaskNotiEnabled = false;
            this.isNewMemberNotiEnabled = false;
            this.isTaskCompleteNotiEnabled = false;
            return;
        }
        if (isNewTaskNotiEnabled != null) this.isNewTaskNotiEnabled = isNewTaskNotiEnabled;
        if (isNewMemberNotiEnabled != null) this.isNewMemberNotiEnabled = isNewMemberNotiEnabled;
        if (isTaskCompleteNotiEnabled != null) this.isTaskCompleteNotiEnabled = isTaskCompleteNotiEnabled;
    }
}
