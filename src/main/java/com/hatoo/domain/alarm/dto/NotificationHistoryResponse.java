package com.hatoo.domain.alarm.dto;

import com.hatoo.domain.alarm.AlarmType;
import com.hatoo.domain.alarm.NotificationHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Schema(description = "알림 내역 응답")
public class NotificationHistoryResponse {

    @Schema(description = "알림 ID")
    private final UUID id;

    @Schema(description = "알림 유형", example = "String")
    private final AlarmType type;

    @Schema(description = "알림 유형 한글명", example = "String")
    private final String typeLabel;

    @Schema(description = "알림 제목", example = "String")
    private final String title;

    @Schema(description = "알림 내용", example = "String")
    private final String body;

    @Schema(description = "읽음 여부", example = "false")
    private final Boolean isRead;

    @Schema(description = "수신 시각")
    private final LocalDateTime createdAt;

    public NotificationHistoryResponse(NotificationHistory n) {
        this.id = n.getId();
        this.type = n.getType();
        this.typeLabel = n.getType().getTitle();
        this.title = n.getTitle();
        this.body = n.getBody();
        this.isRead = n.getIsRead();
        this.createdAt = n.getCreatedAt();
    }
}
