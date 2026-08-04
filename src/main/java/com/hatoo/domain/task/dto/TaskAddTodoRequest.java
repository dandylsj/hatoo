package com.hatoo.domain.task.dto;

import com.hatoo.domain.task.DeadLine;
import com.hatoo.domain.task.Frequency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Schema(description = "할 일 생성/수정 요청")
public class TaskAddTodoRequest {

    @Schema(description = "할 일 제목")
    private String title;

    @Schema(description = "할 일 설명")
    private String description;

    @Schema(description = "담당자 사용자 ID 목록")
    private List<UUID> assigneeIds;

    @Schema(description = "할 일이 속한 그룹 ID")
    private UUID groupId;

    @Schema(description = "반복 주기 (NONE, HOURLY, DAILY, WEEKLY, MONTHLY)")
    private Frequency frequency;

    @Schema(description = "반복 간격 (frequency 단위 기준 몇 번마다 반복할지)")
    private Integer interval;

    @Schema(description = "시작 일시")
    private String dueFrom;

    @Schema(description = "마감 일시")
    private String dueTo;

    @Schema(description = "마감 임박 알림 시점 (NONE, MIN_10, MIN_30, HOUR_1, DAY_1, WEEK_1)")
    private DeadLine deadLine;

    @Schema(description = "시작 알림 수신 여부")
    private Boolean starter;
}
