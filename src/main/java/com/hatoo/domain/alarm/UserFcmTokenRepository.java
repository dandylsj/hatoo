package com.hatoo.domain.alarm;

import com.hatoo.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {

    List<UserFcmToken> findByUser(User user);

    Optional<UserFcmToken> findByFcmToken(String fcmToken);

    void deleteByFcmToken(String fcmToken);

    void deleteByUser(User user);
}
