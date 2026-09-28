package com.hatoo.domain.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 알림 시각 컬럼(start/deadline/overdue_alarm_at)이 추가되기 전에 저장된 미완료 할일의 알림 시각을
 * 기동 시 한 번 채운다. 이미 채워진 할일은 조회 대상이 아니라서 재기동해도 다시 처리하지 않는다(멱등).
 * 이게 없으면 기존 할일은 알림 시각이 null이라 AlarmScheduler 조회에 안 걸려서 알림이 안 간다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskAlarmTimeBackfill implements ApplicationRunner {

    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Task> tasks = taskRepository.findUnfinishedTasksWithoutAlarmTimes();
        if (tasks.isEmpty()) return;
        tasks.forEach(Task::recalculateAlarmTimes);
        log.info("[TaskAlarmTimeBackfill] 기존 미완료 할일 {}건의 알림 시각 계산 완료", tasks.size());
    }
}
