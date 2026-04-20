package com.hatoo.domain.task;

import com.hatoo.domain.groupMember.GroupMember;
import com.hatoo.domain.groupMember.GroupMemberRepository;
import com.hatoo.domain.groups.Group;
import com.hatoo.domain.groups.GroupRepository;
import com.hatoo.domain.weeklyStats.WeeklyStats;
import com.hatoo.domain.weeklyStats.WeeklyStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecurringTaskScheduler {

    private final TaskRepository taskRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final WeeklyStatsRepository weeklyStatsRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Scheduled(cron = "0 0 0 * * *")
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

    // 주차 통계 스냅샷 저장
    // TODO: 테스트 완료 후 "0 */10 * * * *" → "59 59 23 * * SUN" 으로 변경
    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void saveWeeklyStats() {

        LocalDate today = LocalDate.now(KST);
        String weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).format(FORMATTER);
        String weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).format(FORMATTER);

        log.info("[WeeklyStats] 주차 통계 저장 시작 - {}~{}", weekStart, weekEnd);

        List<Group> allGroups = groupRepository.findAll();

        for (Group group : allGroups) {

            // 이번 주 기존 데이터 삭제 후 재저장 (10분마다 최신 상태로 갱신)
            weeklyStatsRepository.deleteByGroupIdAndWeekStart(group.getId(), weekStart);

            // 이번 주 할일 집계 (월요일 00:00 ~ 일요일 23:59:59 기준)
            List<Object[]> results = taskRepository.countFinishedTasksByGroupIdThisWeek(group.getId(), weekStart, weekEnd);

            // userId → [myFinished, groupTotal] 맵
            Map<UUID, int[]> statsMap = new HashMap<>();
            for (Object[] row : results) {
                UUID userId = (UUID) row[0];
                int myFinished = row[3] != null ? ((Number) row[3]).intValue() : 0;
                int groupTotal = row[4] != null ? ((Number) row[4]).intValue() : 0;
                statsMap.put(userId, new int[]{myFinished, groupTotal});
            }

            // 그룹 전체 멤버 저장 (할일 없는 멤버는 0%로)
            List<GroupMember> members = groupMemberRepository.findByGroupId(group.getId());
            for (GroupMember gm : members) {
                int[] stats = statsMap.getOrDefault(gm.getUser().getId(), new int[]{0, 0});
                int myFinished = stats[0];
                int groupTotal = stats[1];
                // 기여도 = 내가 완료한 수 / 그룹 전체 완료된 수 * 100
                int percent = groupTotal > 0 ? (int) Math.round(myFinished * 100.0 / groupTotal) : 0;

                WeeklyStats weeklyStats = new WeeklyStats(
                        group.getId(),
                        gm.getUser().getId(),
                        gm.getUser().getNickname(),
                        gm.getProfileImg(),
                        weekStart,
                        weekEnd,
                        groupTotal,
                        myFinished,
                        percent
                );
                weeklyStatsRepository.save(weeklyStats);
            }

            log.info("[WeeklyStats] 그룹 통계 저장 완료 - groupId: {}, 멤버 수: {}", group.getId(), members.size());
        }

        log.info("[WeeklyStats] 전체 주차 통계 저장 완료");
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
