package com.hatoo.domain.task;

import com.hatoo.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "task_assignees")
@Getter
@NoArgsConstructor
public class TaskAssignee {

    @EmbeddedId
    private TaskAssigneeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taskId")
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private Boolean finished = false;

    @Column
    private LocalDateTime finishedAt;

    public TaskAssignee(Task task, User user) {
        this.id = new TaskAssigneeId(task.getId(), user.getId());
        this.task = task;
        this.user = user;
        this.finished = false;
    }

    public void toggleFinished() {
        this.finished = !Boolean.TRUE.equals(this.finished);
        this.finishedAt = Boolean.TRUE.equals(this.finished)
                ? LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                : null;
    }
}
