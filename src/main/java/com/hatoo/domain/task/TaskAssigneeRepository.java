package com.hatoo.domain.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskAssigneeRepository extends JpaRepository<TaskAssignee, TaskAssigneeId> {

    @Query("SELECT ta FROM TaskAssignee ta JOIN FETCH ta.user WHERE ta.task.id = :taskId")
    List<TaskAssignee> findByTaskId(@Param("taskId") UUID taskId);

    @Query("SELECT ta FROM TaskAssignee ta WHERE ta.task.id = :taskId AND ta.user.id = :userId")
    Optional<TaskAssignee> findByTaskIdAndUserId(@Param("taskId") UUID taskId, @Param("userId") UUID userId);

    void deleteByTaskId(UUID taskId);

    @Modifying
    @Query("DELETE FROM TaskAssignee ta WHERE ta.task.id IN :taskIds")
    void deleteByTaskIdIn(@Param("taskIds") List<UUID> taskIds);

    // 회원탈퇴 시 해당 유저의 모든 task_assignees 레코드 제거 (FK 제약 해소용)
    @Modifying
    @Query("DELETE FROM TaskAssignee ta WHERE ta.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
}
