package com.hatoo.domain.task;

import com.hatoo.domain.groups.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    // 이번 주(weekStart~weekEnd) 그룹 내 담당자별 완료율 집계
    // [userId, nickname, profileImg, finishedCount, totalCount] 순서로 반환
    // 완료율 = 내가 완료한 수 / 내게 할당된 총 할일 수 * 100
    @Query("SELECT u.id, u.nickname, gm.profileImg, " +
           "SUM(CASE WHEN t.finished = true THEN 1 ELSE 0 END), " +
           "COUNT(t) " +
           "FROM Task t " +
           "JOIN t.assignees u " +
           "JOIN t.groups g " +
           "JOIN GroupMember gm ON gm.user.id = u.id AND gm.group.id = g.id " +
           "WHERE g.id = :groupId " +
           "AND SUBSTRING(t.dueFrom, 1, 10) >= :weekStart " +
           "AND SUBSTRING(t.dueFrom, 1, 10) <= :weekEnd " +
           "GROUP BY u.id, u.nickname, gm.profileImg")
    List<Object[]> countFinishedTasksByGroupIdThisWeek(
            @Param("groupId") UUID groupId,
            @Param("weekStart") String weekStart,
            @Param("weekEnd") String weekEnd);

    @Query("SELECT u.id, u.nickname, gm.profileImg, " +
            "SUM(CASE WHEN t.finished = true THEN 1 ELSE 0 END), " +
            "COUNT(t) " +
            "FROM Task t " +
            "JOIN t.assignees u " +
            "JOIN t.groups g " +
            "JOIN GroupMember gm ON gm.user.id = u.id AND gm.group.id = g.id " +
            "WHERE g.id = :groupId " +
            "GROUP BY u.id, u.nickname, gm.profileImg")
    List<Object[]> countFinishedTasksByGroupId(@Param("groupId") UUID groupId);
}
