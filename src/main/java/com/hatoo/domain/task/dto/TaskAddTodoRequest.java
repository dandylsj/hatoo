package com.hatoo.domain.task.dto;

import com.hatoo.domain.task.DeadLine;
import com.hatoo.domain.task.Frequency;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class TaskAddTodoRequest {

    private String title;

    private String description;

    private List<UUID> assigneeIds;

    private UUID groupId;

    private Frequency frequency;

    private Integer interval;

    private String dueFrom;

    private String dueTo;

    private DeadLine deadLine;

    private Boolean starter;
}
