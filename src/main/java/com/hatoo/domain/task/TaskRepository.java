package com.hatoo.domain.task;

import com.hatoo.domain.groups.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByGroupsId(UUID groupId);

    List<Task> findAllByGroupsContainingAndFinishedTrue(Group group);

    // 오늘 마감이고 반복 설정된 Task 조회 (NONE 제외)
    @Query("SELECT t FROM Task t WHERE t.frequency != 'NONE' AND t.dueTo = :dueTo AND t.frequency IS NOT NULL")
    List<Task> findRecurringTasksDueOn(@org.springframework.data.repository.query.Param("dueTo") String dueTo);

}
