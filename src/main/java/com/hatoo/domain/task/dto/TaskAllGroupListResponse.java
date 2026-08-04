package com.hatoo.domain.task.dto;

import com.hatoo.domain.task.DeadLine;
import com.hatoo.domain.task.Frequency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Schema(description = "그룹 내 전체 할 일 목록 응답 (미완료/완료 분리)")
public class TaskAllGroupListResponse {

    @Schema(description = "미완료 할 일 목록")
    private List<TaskList> tasks;

    @Schema(description = "완료된 할 일 목록")
    private List<FinishedTaskList> finishedTasks;

    @Schema(description = "전체 할 일 개수", example = "0")
    private Integer totalCount;

    @Schema(description = "완료된 할 일 개수", example = "0")
    private Integer finishedCount;

    @Getter
    @AllArgsConstructor
    @Schema(description = "미완료 할 일")
    public static class TaskList {

        @Schema(description = "생성일시")
        private LocalDateTime createdAt;

        @Schema(description = "수정일시")
        private LocalDateTime updatedAt;

        @Schema(description = "할 일 ID")
        private UUID id;

        @Schema(description = "할 일 제목")
        private String title;

        @Schema(description = "할 일 설명")
        private String description;

        @Schema(description = "할 일이 속한 그룹 ID")
        private UUID groupId;

        @Schema(description = "시작 일시")
        private String dueFrom;

        @Schema(description = "마감 일시")
        private String dueTo;

        @Schema(description = "담당자 전원 완료 여부")
        private Boolean allFinished;

        @Schema(description = "전원 완료된 일시")
        private LocalDateTime finishedAt;

        @Schema(description = "반복 주기 (NONE, HOURLY, DAILY, WEEKLY, MONTHLY)")
        private Frequency frequency;

        @Schema(description = "반복 간격 (frequency 단위 기준 몇 번마다 반복할지)")
        private Integer interval;

        @Schema(description = "시작 알림 수신 여부")
        private Boolean starter;

        @Schema(description = "마감 임박 알림 시점 (NONE, MIN_10, MIN_30, HOUR_1, DAY_1, WEEK_1)")
        private DeadLine deadLine;

        @Schema(description = "담당자 목록")
        private List<AssigneeDto> assignees;

        @Schema(description = "반복 생성된 원본 할 일 ID (반복 할 일인 경우)")
        private String recurringTaskId;

        @Schema(description = "할 일을 생성한 사용자 ID")
        private UUID creatorId;

        @Getter
        @AllArgsConstructor
        @Schema(description = "담당자 완료 현황")
        public static class AssigneeDto {

            @Schema(description = "담당자 사용자 ID")
            private UUID id;

            @Schema(description = "담당자 닉네임")
            private String nickname;

            @Schema(description = "완료 여부")
            private Boolean finished;

            @Schema(description = "완료일시")
            private LocalDateTime finishedAt;
        }
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "완료된 할 일")
    public static class FinishedTaskList {

        @Schema(description = "생성일시")
        private LocalDateTime createdAt;

        @Schema(description = "수정일시")
        private LocalDateTime updatedAt;

        @Schema(description = "할 일 ID")
        private UUID id;

        @Schema(description = "할 일 제목")
        private String title;

        @Schema(description = "할 일 설명")
        private String description;

        @Schema(description = "할 일이 속한 그룹 ID")
        private UUID groupId;

        @Schema(description = "시작 일시")
        private String dueFrom;

        @Schema(description = "마감 일시")
        private String dueTo;

        @Schema(description = "담당자 전원 완료 여부")
        private Boolean allFinished;

        @Schema(description = "전원 완료된 일시")
        private LocalDateTime finishedAt;

        @Schema(description = "반복 주기 (NONE, HOURLY, DAILY, WEEKLY, MONTHLY)")
        private Frequency frequency;

        @Schema(description = "반복 간격 (frequency 단위 기준 몇 번마다 반복할지)")
        private Integer interval;

        @Schema(description = "시작 알림 수신 여부")
        private Boolean starter;

        @Schema(description = "마감 임박 알림 시점 (NONE, MIN_10, MIN_30, HOUR_1, DAY_1, WEEK_1)")
        private DeadLine deadLine;

        @Schema(description = "담당자 목록")
        private List<TaskList.AssigneeDto> assignees;

        @Schema(description = "반복 생성된 원본 할 일 ID (반복 할 일인 경우)")
        private String recurringTaskId;

        @Schema(description = "할 일을 생성한 사용자 ID")
        private UUID creatorId;
    }
}
