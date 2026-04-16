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

    // 특정 유저가 담당자로 있는 할일 조회
    List<Task> findByAssigneesId(UUID userId);

    // 특정 그룹에서 특정 유저가 담당인 할일 조회
    List<Task> findByAssigneesIdAndGroupsId(UUID userId, UUID groupId);

    List<Task> findAllByGroupsContainingAndFinishedTrue(Group group);

    // dueFrom ~ dueTo 범위 안에 오늘이 포함되는 반복 할일 조회 (NONE 제외)
    // SUBSTRING으로 앞 10자리만 비교 → "2026-04-20T17:02:28.613Z" 같은 ISO 형식도 처리 가능
    @Query("SELECT t FROM Task t WHERE t.frequency IS NOT NULL AND t.frequency != com.hatoo.domain.task.Frequency.NONE AND SUBSTRING(t.dueFrom, 1, 10) <= :today AND SUBSTRING(t.dueTo, 1, 10) >= :today")
    List<Task> findRecurringTasksDueOn(@org.springframework.data.repository.query.Param("today") String today);

    // 중복 생성 방지: 같은 반복그룹에 동일한 dueTo를 가진 할일이 이미 있는지 확인
    boolean existsByRecurringTaskIdAndDueTo(String recurringTaskId, String dueTo);

    // 그룹 내 담당자별 완료 할일 수 + 그룹 전체 할일 수 집계 (완료율 계산용)
    // [userId, nickname, profileImg, userFinishedCount, groupTotalCount] 순서로 반환
    // 완료율 = 내가 완료한 수 / 그룹 전체 할일 수 * 100
    @Query("SELECT u.id, u.nickname, gm.profileImg, " +
           "SUM(CASE WHEN t.finished = true THEN 1 ELSE 0 END), " +
           "(SELECT COUNT(t2) FROM Task t2 JOIN t2.groups g2 WHERE g2.id = :groupId) " +
           "FROM Task t " +
           "JOIN t.assignees u " +
           "JOIN t.groups g " +
           "JOIN GroupMember gm ON gm.user.id = u.id AND gm.group.id = g.id " +
           "WHERE g.id = :groupId " +
           "GROUP BY u.id, u.nickname, gm.profileImg " +
           "ORDER BY SUM(CASE WHEN t.finished = true THEN 1 ELSE 0 END) DESC")
    List<Object[]> countFinishedTasksByGroupId(@Param("groupId") UUID groupId);

}
