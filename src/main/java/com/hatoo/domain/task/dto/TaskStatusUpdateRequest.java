package com.hatoo.domain.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "할 일 완료 상태 변경 요청")
public class TaskStatusUpdateRequest {

    @Schema(description = "완료 여부")
    private Boolean taskStatus;

    public boolean getTaskStatus() {
        return taskStatus;
    }
}
