package com.hatoo.domain.task;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssigneeId implements Serializable {

    @Column(name = "task_id", columnDefinition = "BINARY(16)")
    private UUID taskId;

    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID userId;
}
