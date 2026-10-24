package com.hatoo.domain.task;

import com.hatoo.domain.groups.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByGroupsId(UUID groupId);

    // 담당자 + 유저 정보 fetch join 으로 N+1 방지
    @Query("SELECT DISTINCT t FROM Task t " +
           "LEFT JOIN FETCH t.taskAssignees ta " +
           "LEFT JOIN FETCH ta.user " +
           "JOIN t.groups g WHERE g.id = :groupId " +
           "ORDER BY CASE WHEN t.dueTo IS NULL THEN 1 ELSE 0 END ASC, t.dueTo ASC")
    List<Task> findByGroupsIdOrderByDueToDesc(@Param("groupId") UUID groupId);

    @Query("SELECT DISTINCT t FROM Task t JOIN t.taskAssignees ta WHERE ta.user.id = :userId")
    List<Task> findByAssigneesId(@Param("userId") UUID userId);

    @Query("SELECT DISTINCT t FROM Task t JOIN t.taskAssignees ta JOIN t.groups g WHERE ta.user.id = :userId AND g.id = :groupId")
    List<Task> findByAssigneesIdAndGroupsId(@Param("userId") UUID userId, @Param("groupId") UUID groupId);

    @Query("SELECT DISTINCT t FROM Task t JOIN t.groups g WHERE t.creatorId = :userId AND g.id = :groupId")
    List<Task> findByCreatorIdAndGroupsId(@Param("userId") UUID userId, @Param("groupId") UUID groupId);

    List<Task> findAllByGroupsContainingAndFinishedTrue(Group group);

    // 반복 할일 조회
    @Query("SELECT t FROM Task t WHERE t.frequency IS NOT NULL AND t.frequency != com.hatoo.domain.task.Frequency.NONE " +
           "AND SUBSTRING(t.dueFrom, 1, 10) <= :today AND SUBSTRING(t.dueTo, 1, 10) >= :today")
    List<Task> findRecurringTasksDueOn(@Param("today") String today);

    // 중복 생성 방지
    boolean existsByRecurringTaskIdAndDueTo(String recurringTaskId, String dueTo);

    // 알림 스케줄러용
    List<Task> findByDueFromStartingWith(String date);
    List<Task> findByDueToStartingWith(String date);

    // AlarmScheduler용: 알림 시각이 [from, to) 구간(= 이번 1분)인 할일만 인덱스로 조회한다.
    // 예전에는 미완료 할일 전체를 불러와 dueFrom/dueTo 문자열을 자바에서 파싱해 걸렀다 (TaskAlarmTimes 참고).
    // 발송 대상은 어차피 담당자 목록을 읽으므로 담당자+유저를 fetch join 한다.
    @Query("SELECT DISTINCT t FROM Task t " +
           "LEFT JOIN FETCH t.taskAssignees ta LEFT JOIN FETCH ta.user " +
           "WHERE t.startAlarmAt >= :from AND t.startAlarmAt < :to " +
           "AND t.starter = true AND t.finished = false AND t.startAlarmSent = false")
    List<Task> findStartAlarmTargets(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT DISTINCT t FROM Task t " +
           "LEFT JOIN FETCH t.taskAssignees ta LEFT JOIN FETCH ta.user " +
           "WHERE t.deadlineAlarmAt >= :from AND t.deadlineAlarmAt < :to " +
           "AND t.finished = false AND t.deadlineAlarmSent = false")
    List<Task> findDeadlineAlarmTargets(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT DISTINCT t FROM Task t " +
           "LEFT JOIN FETCH t.taskAssignees ta LEFT JOIN FETCH ta.user " +
           "WHERE t.overdueAlarmAt >= :from AND t.overdueAlarmAt < :to " +
           "AND t.finished = false AND t.overdueAlarmSent = false")
    List<Task> findOverdueAlarmTargets(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // 알림 시각 컬럼 추가 전에 저장된 미완료 할일 백필용 (TaskAlarmTimeBackfill)
    @Query("SELECT t FROM Task t WHERE t.finished = false " +
           "AND t.startAlarmAt IS NULL AND t.overdueAlarmAt IS NULL " +
           "AND (t.dueFrom IS NOT NULL OR t.dueTo IS NOT NULL)")
    List<Task> findUnfinishedTasksWithoutAlarmTimes();

    // 이번 주 그룹 내 담당자별 기여도 집계 (TaskAssignee.finished 기준)
    @Query("SELECT ta.user.id, ta.user.nickname, gm.profileImg, " +
           "SUM(CASE WHEN ta.finished = true AND ta.finishedAt IS NOT NULL " +
           "    AND CAST(ta.finishedAt AS date) >= :weekStart " +
           "    AND CAST(ta.finishedAt AS date) <= :weekEnd THEN 1 ELSE 0 END), " +
           "(SELECT COUNT(ta2) FROM TaskAssignee ta2 JOIN ta2.task t2 JOIN t2.groups g2 WHERE g2.id = :groupId " +
           " AND ta2.finished = true AND ta2.finishedAt IS NOT NULL " +
           " AND CAST(ta2.finishedAt AS date) >= :weekStart " +
           " AND CAST(ta2.finishedAt AS date) <= :weekEnd) " +
           "FROM TaskAssignee ta " +
           "JOIN ta.task t " +
           "JOIN t.groups g " +
           "JOIN GroupMember gm ON gm.user.id = ta.user.id AND gm.group.id = g.id " +
           "WHERE g.id = :groupId " +
           "GROUP BY ta.user.id, ta.user.nickname, gm.profileImg")
    List<Object[]> countFinishedTasksByGroupIdThisWeek(
            @Param("groupId") UUID groupId,
            @Param("weekStart") LocalDate weekStart,
            @Param("weekEnd") LocalDate weekEnd);
}