package com.hatoo.domain.task.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TaskStatusUpdateResponse {

    private Boolean taskStatus;
    private LocalDateTime finishedAt;  // 완료 시각 (완료 취소 시 null)
}
