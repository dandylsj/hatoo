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
public class TaskAllGroupListResponse {

    private List<TaskList> tasks;
    private List<FinishedTaskList> finishedTasks;

    @Schema(example = "0")
    private Integer totalCount;

    @Schema(example = "0")
    private Integer finishedCount;

    @Getter
    @AllArgsConstructor
    public static class TaskList {

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private UUID id;
        private String title;
        private String description;
        private UUID groupId;
        private String dueFrom;
        private String dueTo;
        private Boolean allFinished;
        private LocalDateTime finishedAt;
        private Frequency frequency;
        private Integer interval;
        private Boolean starter;
        private DeadLine deadLine;
        private List<AssigneeDto> assignees;
        private String recurringTaskId;
        private UUID creatorId;

        @Getter
        @AllArgsConstructor
        public static class AssigneeDto {
            private UUID id;
            private String nickname;
            private Boolean finished;
            private LocalDateTime finishedAt;
        }
    }

    @Getter
    @AllArgsConstructor
    public static class FinishedTaskList {

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private UUID id;
        private String title;
        private String description;
        private UUID groupId;
        private String dueFrom;
        private String dueTo;
        private Boolean allFinished;
        private LocalDateTime finishedAt;
        private Frequency frequency;
        private Integer interval;
        private Boolean starter;
        private DeadLine deadLine;
        private List<TaskList.AssigneeDto> assignees;
        private String recurringTaskId;
        private UUID creatorId;
    }
}
