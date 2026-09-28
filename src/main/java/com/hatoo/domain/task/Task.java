package com.hatoo.domain.task;

import com.hatoo.common.BaseEntity;
import com.hatoo.domain.groups.Group;
import com.hatoo.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "tasks", indexes = {
        @Index(name = "idx_tasks_start_alarm_at", columnList = "start_alarm_at"),
        @Index(name = "idx_tasks_deadline_alarm_at", columnList = "deadline_alarm_at"),
        @Index(name = "idx_tasks_overdue_alarm_at", columnList = "overdue_alarm_at")
})
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

    // 완료 처리 시각 (KST), 완료 취소 시 null
    @Column
    private LocalDateTime finishedAt;

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

    // ──────────────────────────────────────────
    // 알림 발송 여부 플래그 (중복 알림 방지)
    // ──────────────────────────────────────────

    @Column(name = "start_alarm_sent")
    private Boolean startAlarmSent = false;

    @Column(name = "deadline_alarm_sent")
    private Boolean deadlineAlarmSent = false;

    @Column(name = "overdue_alarm_sent")
    private Boolean overdueAlarmSent = false;

    // ──────────────────────────────────────────
    // 알림 발송 시각 (KST, 분 단위) - dueFrom/dueTo 문자열을 저장 시점에 한 번만 파싱해둔 값.
    // AlarmScheduler가 매 분 "이 1분에 보낼 알림"을 인덱스로 바로 찾기 위해 쓴다 (TaskAlarmTimes 참고).
    // ──────────────────────────────────────────

    @Column(name = "start_alarm_at")
    private LocalDateTime startAlarmAt;

    @Column(name = "deadline_alarm_at")
    private LocalDateTime deadlineAlarmAt;

    @Column(name = "overdue_alarm_at")
    private LocalDateTime overdueAlarmAt;

    @Column(columnDefinition = "BINARY(16)")
    private UUID creatorId;

    // 담당자별 완료 상태 포함 (Direction 2)
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TaskAssignee> taskAssignees = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @BatchSize(size = 100)
    @JoinTable(
            name = "group_tasks",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private List<Group> groups = new ArrayList<>();

    public Task(String title, String description, Frequency frequency, String dueFrom, String dueTo, DeadLine deadLine, Boolean starter, Integer interval) {
        this.title = title;
        this.description = description;
        this.frequency = frequency;
        this.dueFrom = dueFrom;
        this.dueTo = dueTo;
        this.deadLine = deadLine;
        this.starter = starter;
        this.interval = interval;
        recalculateAlarmTimes();
    }

    // 편의 메서드: User 목록 반환 (AlarmScheduler 등 기존 호출부 호환)
    public List<User> getAssignees() {
        return taskAssignees.stream()
                .map(TaskAssignee::getUser)
                .collect(Collectors.toList());
    }

    public void addGroup(Group group) {
        this.groups.add(group);
    }

    public UUID getGroupId() {
        return this.groups.get(0).getId();
    }

public void updateTask(String title, String description, Frequency frequency, Integer interval, String dueFrom, String dueTo, DeadLine deadLine, Boolean starter) {
        this.title = title;
        this.description = description;
        this.frequency = frequency;
        this.interval = interval;
        this.deadLine = deadLine;
        this.starter = starter;

        // 시작 시간이 바뀌면 시작 알람 플래그 리셋 → 새 시간에 알람 재발송
        if (dueFrom != null && !dueFrom.equals(this.dueFrom)) {
            this.startAlarmSent = false;
        }
        this.dueFrom = dueFrom;

        // 마감 시간 또는 마감 설정이 바뀌면 마감/초과 알람 플래그 리셋
        boolean dueToChanged = dueTo != null && !dueTo.equals(this.dueTo);
        boolean deadLineChanged = deadLine != null && !deadLine.equals(this.deadLine);
        if (dueToChanged || deadLineChanged) {
            this.deadlineAlarmSent = false;
            this.overdueAlarmSent = false;
        }
        this.dueTo = dueTo;
        recalculateAlarmTimes();
    }

    /** dueFrom/dueTo/deadLine이 바뀔 때마다 알림 발송 시각을 다시 계산한다. 기존 데이터 백필에도 쓴다. */
    public void recalculateAlarmTimes() {
        this.startAlarmAt = TaskAlarmTimes.startAlarmAt(this.dueFrom);
        this.deadlineAlarmAt = TaskAlarmTimes.deadlineAlarmAt(this.dueTo, this.deadLine);
        this.overdueAlarmAt = TaskAlarmTimes.overdueAlarmAt(this.dueTo);
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
        // 완료 처리 시 현재 KST 시각 기록, 완료 취소 시 null로 초기화
        this.finishedAt = finished ? LocalDateTime.now(ZoneId.of("Asia/Seoul")) : null;
    }

    public void setRecurringTaskId(String recurringTaskId) {
        this.recurringTaskId = recurringTaskId;
    }

    public void markStartAlarmSent() {
        this.startAlarmSent = true;
    }

    public void markDeadlineAlarmSent() {
        this.deadlineAlarmSent = true;
    }

    public void markOverdueAlarmSent() {
        this.overdueAlarmSent = true;
    }

    public void setCreatorId(UUID creatorId) {
        this.creatorId = creatorId;
    }
}
