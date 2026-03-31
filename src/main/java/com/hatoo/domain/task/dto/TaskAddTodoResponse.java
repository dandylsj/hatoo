package com.hatoo.domain.task.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class TaskAddTodoResponse {

    private UUID id;

    private String title;

    private String description;

    private UUID groupId;

    private String dueFrom;

    private String dueTo;

    private Boolean finished;

    private String recurringTaskId;

    private AssigneeDto assignee;

    @Getter
    @AllArgsConstructor
    public static class AssigneeDto {
        private String nickname;
    }
}
