package com.hatoo.domain.alarm;

import com.hatoo.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {

    List<UserFcmToken> findByUser(User user);

    Optional<UserFcmToken> findByFcmToken(String fcmToken);

    void deleteByFcmToken(String fcmToken);

    void deleteByUser(User user);

    // 중복 키 충돌 시 예외 없이 무시 (race condition 방어)
    @Modifying
    @Transactional
    @Query(value = "INSERT IGNORE INTO user_fcm_tokens (created_at, device_type, fcm_token, user_id) " +
                   "VALUES (NOW(), :deviceType, :fcmToken, UUID_TO_BIN(:userId))",
           nativeQuery = true)
    void saveIgnore(@Param("userId") String userId,
                    @Param("fcmToken") String fcmToken,
                    @Param("deviceType") String deviceType);
}
