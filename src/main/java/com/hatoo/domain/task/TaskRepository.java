package com.hatoo.domain.task;

import com.hatoo.domain.groups.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByGroupsId(UUID groupId);

    List<Task> findByAssigneesId(UUID userId);

    List<Task> findByAssigneesIdAndGroupsId(UUID userId, UUID groupId);

    List<Task> findAllByGroupsContainingAndFinishedTrue(Group group);

    // 반복 할일 조회 (오늘 날짜가 dueFrom~dueTo 범위 안에 있는 것)
    @Query("SELECT t FROM Task t WHERE t.frequency IS NOT NULL AND t.frequency != com.hatoo.domain.task.Frequency.NONE " +
           "AND SUBSTRING(t.dueFrom, 1, 10) <= :today AND SUBSTRING(t.dueTo, 1, 10) >= :today")
    List<Task> findRecurringTasksDueOn(@Param("today") String today);

    // 중복 생성 방지
    boolean existsByRecurringTaskIdAndDueTo(String recurringTaskId, String dueTo);

    // 알림 스케줄러용 - dueFrom이 특정 날짜로 시작하는 할일 조회
    List<Task> findByDueFromStartingWith(String date);

    // 알림 스케줄러용 - dueTo가 특정 날짜로 시작하는 할일 조회
    List<Task> findByDueToStartingWith(String date);

    // ──────────────────────────────────────────
    // 알림 스케줄러용 (개선된 버전)
    // ──────────────────────────────────────────

    // 1. 시작 알림: 미완료 + 시작 알림 미발송 전체 조회 (dueTo 시각 비교는 코드에서 처리)
    List<Task> findByFinishedFalseAndStartAlarmSentFalse();

    // 2. 마감 임박 알림: deadLine이 설정됐고, 미완료, 마감임박 알림 미발송
    @Query("SELECT t FROM Task t WHERE t.deadLine IS NOT NULL " +
           "AND t.deadLine != com.hatoo.domain.task.DeadLine.NONE " +
           "AND t.finished = false " +
           "AND t.deadlineAlarmSent = false")
    List<Task> findTasksForDeadlineAlarm();

    // 3. 마감 초과 알림: 미완료, 마감초과 알림 미발송
    List<Task> findByFinishedFalseAndOverdueAlarmSentFalse();

    // 이번 주(weekStart~weekEnd) 그룹 내 담당자별 기여도 집계
    // [userId, nickname, profileImg, myFinishedCount, groupTotalFinishedCount] 순서로 반환
    // 기여도 = 내가 완료한 수 / 그룹 전체 완료된 수 * 100
    // finishedAt 기준으로 이번 주에 완료된 할일을 집계
    @Query("SELECT u.id, u.nickname, gm.profileImg, " +
           "SUM(CASE WHEN t.finished = true AND t.finishedAt IS NOT NULL " +
           "    AND CAST(t.finishedAt AS date) >= :weekStart " +
           "    AND CAST(t.finishedAt AS date) <= :weekEnd THEN 1 ELSE 0 END), " +
           "(SELECT COUNT(t2) FROM Task t2 JOIN t2.groups g2 WHERE g2.id = :groupId " +
           " AND t2.finished = true AND t2.finishedAt IS NOT NULL " +
           " AND CAST(t2.finishedAt AS date) >= :weekStart " +
           " AND CAST(t2.finishedAt AS date) <= :weekEnd) " +
           "FROM Task t " +
           "JOIN t.assignees u " +
           "JOIN t.groups g " +
           "JOIN GroupMember gm ON gm.user.id = u.id AND gm.group.id = g.id " +
           "WHERE g.id = :groupId " +
           "GROUP BY u.id, u.nickname, gm.profileImg")
    List<Object[]> countFinishedTasksByGroupIdThisWeek(
            @Param("groupId") UUID groupId,
            @Param("weekStart") LocalDate weekStart,
            @Param("weekEnd") LocalDate weekEnd);
}
