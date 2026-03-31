package com.hatoo.domain.task.dto;

import com.hatoo.domain.task.Frequency;
import lombok.Getter;

import java.util.UUID;

@Getter
public class TaskAddTodoRequest {

    private String title;

    private String description;

    private UUID assigneeId;

    private UUID groupId;

    private Frequency frequency;

    private Integer interval;

    private String dueFrom;

    private String dueTo;

    private String deadLine;

    private Boolean starter;
}