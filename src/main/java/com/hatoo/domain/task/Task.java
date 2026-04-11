package com.hatoo.domain.task;

import com.hatoo.common.BaseEntity;
import com.hatoo.domain.groups.Group;
import com.hatoo.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column
    private String title;

    @Column
    private String description;

    @Column
    private String dueTo;

    @Column
    private String dueFrom;

    @Column
    private Boolean finished = false;

    @Enumerated(EnumType.STRING)
    private DeadLine deadLine;

    @Column
    private Boolean starter;

    @Column
    private String recurringTaskId;

    @Column(name = "task_interval")
    private Integer interval;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency")
    private Frequency frequency;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_assignees",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> assignees = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "group_tasks",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private List<Group> groups = new ArrayList<>();

    public Task(String title, String description, Frequency frequency, String dueFrom, String dueTo, DeadLine deadLine, Boolean starter) {
        this.title = title;
        this.description = description;
        this.frequency = frequency;
        this.dueFrom = dueFrom;
        this.dueTo = dueTo;
        this.deadLine = deadLine;
        this.starter = starter;
    }

    public void addAssignee(User user) {
        this.assignees.add(user);
    }

    public void addGroup(Group group) {
        this.groups.add(group);
    }

    public UUID getGroupId() {
        return this.groups.get(0).getId();
    }

    public UUID getAssigneeId() {
        return this.assignees.get(0).getId();
    }


    public void updateTask(String title, String description, Frequency frequency, String dueFrom, String dueTo, DeadLine deadLine, Boolean starter) {
        this.title = title;
        this.description = description;
        this.frequency = frequency;
        this.dueFrom = dueFrom;
        this.dueTo = dueTo;
        this.deadLine = deadLine;
        this.starter = starter;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
