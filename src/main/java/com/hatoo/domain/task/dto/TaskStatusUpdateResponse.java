package com.hatoo.domain.task.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TaskStatusUpdateResponse {

    // 요청한 유저 본인의 완료 여부
    private Boolean myFinished;

    // 요청한 유저 본인의 완료 시각 (미완료 시 null)
    private LocalDateTime myFinishedAt;

    // 모든 담당자가 완료했는지 여부
    private Boolean allFinished;
}
