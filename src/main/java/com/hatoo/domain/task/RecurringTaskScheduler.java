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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final WeeklyStatsRepository weeklyStatsRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DUE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void createRecurringTasks() {

        String today = LocalDate.now(KST).format(FORMATTER);
        log.info("[TaskScheduler] 반복 할일 생성 시작 - 기준일: {}", today);

        // dueTo 가 오늘이고 반복 설정된 Task 조회
        List<Task> recurringTasks = taskRepository.findRecurringTasksDueOn(today);
        log.info("[TaskScheduler] 반복 할일 조회 결과 - {}건 발견", recurringTasks.size());

        recurringTasks.forEach(task -> {
            // 원래 시각(시:분)을 유지한 채 다음 회차 날짜만 계산 (시간이 유실되면 자정 기준으로
            // 재해석되어 마감 임박 알림이 하루 전날 밤에 울리는 문제가 발생함)
            LocalDateTime nextDueTo = calculateNextDate(task.getDueTo(), task.getFrequency(), task.getInterval());
            if (nextDueTo == null) {
                log.error("[TaskScheduler] 날짜 파싱 실패 - taskId: {}, dueTo: {}", task.getId(), task.getDueTo());
                return;
            }
            LocalDateTime nextDueFrom = task.getDueFrom() != null
                    ? calculateNextDate(task.getDueFrom(), task.getFrequency(), task.getInterval())
                    : nextDueTo;
            if (nextDueFrom == null) {
                nextDueFrom = nextDueTo;
            }

            String nextDueToStr = nextDueTo.format(DUE_DATETIME_FORMATTER);
            String nextDueFromStr = nextDueFrom.format(DUE_DATETIME_FORMATTER);

            try {
                Task newTask = new Task(
                        task.getTitle(),
                        task.getDescription(),
                        task.getFrequency(),
                        nextDueFromStr,
                        nextDueToStr,
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
                if (taskRepository.existsByRecurringTaskIdAndDueTo(recurringId, nextDueToStr)) {
                    log.info("[TaskScheduler] 이미 생성된 할일 - title: {}, 다음 마감일: {}", task.getTitle(), nextDueToStr);
                    return;
                }

                // 그룹 & 생성자 복사
                task.getGroups().forEach(newTask::addGroup);
                newTask.setCreatorId(task.getCreatorId());

                taskRepository.save(newTask);

                // 담당자 복사 (task 저장 후 ID가 생성된 뒤 TaskAssignee 생성)
                List<TaskAssignee> originalAssignees = taskAssigneeRepository.findByTaskId(task.getId());
                originalAssignees.forEach(ta -> {
                    TaskAssignee newTa = new TaskAssignee(newTask, ta.getUser());
                    taskAssigneeRepository.save(newTa);
                });
                log.info("[TaskScheduler] 반복 할일 생성 완료 - title: {}, 다음 마감일: {}", task.getTitle(), nextDueToStr);

            } catch (Exception e) {
                log.error("[TaskScheduler] 반복 할일 생성 실패 - taskId: {}, error: {}", task.getId(), e.getMessage());
            }
        });
    }

    // 지난 주 통계 스냅샷 저장 - 매주 월요일 8시 (KST) 실행
    @Scheduled(cron = "0 0 8 * * MON", zone = "Asia/Seoul")
    @Transactional
    public void saveWeeklyStats() {

        LocalDate today = LocalDate.now(KST);
        // 지난 주 월요일 ~ 일요일 (오늘이 월요일이므로 previous(MONDAY) = 7일 전 월요일)
        LocalDate weekStartDate = today.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        String weekStart = weekStartDate.format(FORMATTER);
        String weekEnd = weekEndDate.format(FORMATTER);

        log.info("[WeeklyStats] 지난 주 통계 저장 시작 - {}~{}", weekStart, weekEnd);

        List<Group> allGroups = groupRepository.findAll();

        for (Group group : allGroups) {

            // 기존 데이터 전부 삭제 후 새로 저장 (잘못된 날짜 데이터 포함 항상 초기화)
            weeklyStatsRepository.deleteByGroupId(group.getId());

            // 지난 주 할일 집계
            List<Object[]> results = taskRepository.countFinishedTasksByGroupIdThisWeek(group.getId(), weekStartDate, weekEndDate);

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

                String profileImg = gm.getProfileImg() != null ? gm.getProfileImg() : "";
                WeeklyStats weeklyStats = new WeeklyStats(
                        group.getId(),
                        gm.getUser().getId(),
                        gm.getUser().getNickname(),
                        profileImg,
                        weekStart,
                        weekEnd,
                        groupTotal,
                        myFinished,
                        percent
                );
                weeklyStatsRepository.save(weeklyStats);
            }

            log.info("[WeeklyStats] 그룹 통계 저장 완료 - groupId: {}, weekStart: {}, 멤버 수: {}", group.getId(), weekStart, members.size());
        }

        log.info("[WeeklyStats] 전체 주차 통계 저장 완료 - {}~{}", weekStart, weekEnd);
    }

    private LocalDateTime calculateNextDate(String dateStr, Frequency frequency, Integer interval) {
        LocalDateTime dateTime = TaskAlarmTimes.parse(dateStr);
        if (dateTime == null) return null;
        int amount = interval != null ? interval : 1;

        return switch (frequency) {
            case HOURLY -> dateTime.plusDays(amount);
            case DAILY -> dateTime.plusDays(amount);
            case WEEKLY -> dateTime.plusWeeks(amount);
            case MONTHLY -> dateTime.plusMonths(amount);
            default -> dateTime.plusDays(1);
        };
    }
}