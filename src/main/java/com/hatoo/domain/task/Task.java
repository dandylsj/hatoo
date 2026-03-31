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
    private String finished;

    @Column
    private String deadLine;

    @Column
    private Boolean starter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Column
    private String recurringTaskId;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false)
    private Frequency frequency;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "group_tasks",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private List<Group> groups = new ArrayList<>();



}
