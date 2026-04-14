package com.hatoo.domain.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecurringTaskScheduler {

    private final TaskRepository taskRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void createRecurringTasks() {

        String today = LocalDate.now().format(FORMATTER);
        log.info("[TaskScheduler] 반복 할일 생성 시작 - 기준일: {}", today);

        // dueTo 가 오늘이고 반복 설정된 Task 조회
        List<Task> recurringTasks = taskRepository.findRecurringTasksDueOn(today);
        log.info("[TaskScheduler] 반복 할일 조회 결과 - {}건 발견", recurringTasks.size());

        recurringTasks.forEach(task -> {
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

                // 중복 생성 방지: 동일한 recurringTaskId + nextDueTo 할일이 이미 있으면 건너뜀
                if (taskRepository.existsByRecurringTaskIdAndDueTo(recurringId, nextDueTo.format(FORMATTER))) {
                    log.info("[TaskScheduler] 이미 생성된 할일 - title: {}, 다음 마감일: {}", task.getTitle(), nextDueTo);
                    return;
                }

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
        LocalDate date = LocalDate.parse(normalizeDate(dateStr), FORMATTER);
        int amount = interval != null ? interval : 1;

        return switch (frequency) {
            case DAILY -> date.plusDays(amount);
            case WEEKLY -> date.plusWeeks(amount);
            case MONTHLY -> date.plusMonths(amount);
            default -> date.plusDays(amount);
        };
    }

    // "2026-04-20T17:02:28.613Z" 같은 ISO 형식이 오면 날짜 부분만 추출
    private String normalizeDate(String dateStr) {
        if (dateStr != null && dateStr.contains("T")) {
            return dateStr.substring(0, 10);
        }
        return dateStr;
    }
}
