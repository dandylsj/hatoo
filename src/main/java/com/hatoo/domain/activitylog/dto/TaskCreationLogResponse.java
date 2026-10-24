package com.hatoo.domain.activitylog.dto;

import com.hatoo.domain.activitylog.TaskCreationLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Schema(description = "할일 등록 로그 응답")
public class TaskCreationLogResponse {

    @Schema(description = "할일 ID")
    private final UUID taskId;

    @Schema(description = "등록자(생성자) ID")
    private final UUID creatorId;

    @Schema(description = "등록자 닉네임")
    private final String creatorNickname;

    @Schema(description = "그룹 ID")
    private final UUID groupId;

    @Schema(description = "할일 제목")
    private final String title;

    @Schema(description = "등록 시각")
    private final LocalDateTime createdAt;

    public TaskCreationLogResponse(TaskCreationLog log) {
        this.taskId = log.getTaskId();
        this.creatorId = log.getCreatorId();
        this.creatorNickname = log.getCreatorNickname();
        this.groupId = log.getGroupId();
        this.title = log.getTitle();
        this.createdAt = log.getCreatedAt();
    }
}
