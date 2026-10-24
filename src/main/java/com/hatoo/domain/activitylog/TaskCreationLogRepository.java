package com.hatoo.domain.activitylog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskCreationLogRepository extends JpaRepository<TaskCreationLog, UUID> {

    List<TaskCreationLog> findAllByOrderByCreatedAtDesc();

    List<TaskCreationLog> findByCreatorIdOrderByCreatedAtDesc(UUID creatorId);

    List<TaskCreationLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to);
}
