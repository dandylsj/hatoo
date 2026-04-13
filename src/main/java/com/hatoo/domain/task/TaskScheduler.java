package com.hatoo.domain.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskScheduler {

    private final TaskRepository taskRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 매일 자정 실행
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void createRecurringTasks() {

        String today = LocalDate.now().format(FORMATTER);
        log.info("[TaskScheduler] 반복 할일 생성 시작 - 기준일: {}", today);

        // dueTo 가 오늘이고 반복 설정된 Task 조회
        taskRepository.findRecurringTasksDueOn(today).forEach(task -> {
            try {
                LocalDate nextDueTo = calculateNextDate(task.getDueTo(), task.getFrequency(), task.getInterval());
                LocalDate nextDueFrom = task.getDueFrom() != null
                        ? calculateNextDate(task.getDueFrom(), task.getFrequency(), task.getInterval())
                        : nextDueTo;

                Task newTask = new Task(
                        task.getTitle(),
                        task.getDescription(),
                        task.getFrequency(),
                        nextDueFrom.format(FORMATTER),
                        nextDueTo.format(FORMATTER),
                        task.getDeadLine(),
                        task.getStarter(),
                        task.getInterval()
                );

                // 같은 반복 그룹 id 유지
                String recurringId = task.getRecurringTaskId() != null
                        ? task.getRecurringTaskId()
                        : task.getId().toString();
                newTask.setRecurringTaskId(recurringId);

                // 담당자 & 그룹 복사
                task.getAssignees().forEach(newTask::addAssignee);
                task.getGroups().forEach(newTask::addGroup);

                taskRepository.save(newTask);
                log.info("[TaskScheduler] 반복 할일 생성 완료 - title: {}, 다음 마감일: {}", task.getTitle(), nextDueTo);

            } catch (DateTimeParseException e) {
                log.error("[TaskScheduler] 날짜 파싱 실패 - taskId: {}, dueTo: {}", task.getId(), task.getDueTo());
            }
        });
    }

    private LocalDate calculateNextDate(String dateStr, Frequency frequency, Integer interval) {
        LocalDate date = LocalDate.parse(dateStr, FORMATTER);
        int amount = interval != null ? interval : 1;

        return switch (frequency) {
            case DAILY -> date.plusDays(amount);
            case WEEKLY -> date.plusWeeks(amount);
            case MONTHLY -> date.plusMonths(amount);
            default -> date.plusDays(amount);
        };
    }
}
