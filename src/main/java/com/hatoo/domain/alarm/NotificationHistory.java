package com.hatoo.domain.alarm;

import com.hatoo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "notification_history",
       indexes = @Index(name = "idx_notification_user_id", columnList = "user_id"))
@Getter
@NoArgsConstructor
public class NotificationHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private AlarmType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 500)
    private String body;

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(columnDefinition = "BINARY(16)")
    private UUID taskId;

    public NotificationHistory(UUID userId, AlarmType type, String title, String body, UUID taskId) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.body = body;
        this.isRead = false;
        this.taskId = taskId;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
