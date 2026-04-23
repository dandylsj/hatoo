package com.hatoo.domain.alarm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, UUID> {

    // 내 알림 목록 조회 (최신순)
    List<NotificationHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);

    // 전체 읽음 처리 (벌크 업데이트)
    @Modifying
    @Query("UPDATE NotificationHistory n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") UUID userId);

    // 읽음 처리된 지 3일 이상 된 알림 삭제 (스케줄러용)
    @Modifying
    @Query("DELETE FROM NotificationHistory n WHERE n.isRead = true AND n.updatedAt < :cutoff")
    void deleteReadNotificationsOlderThan(@Param("cutoff") LocalDateTime cutoff);
}
