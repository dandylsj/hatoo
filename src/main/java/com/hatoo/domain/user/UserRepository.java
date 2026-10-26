package com.hatoo.domain.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(@NotBlank(message = "이메일은 필수입니다.") @Email(message = "올바른 이메일 형식이 아닙니다.") String email);

    Optional<User> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);

    Optional<User> findByKakaoId(Long kakaoId);

    Optional<User> findByNaverId(String naverId);

    Optional<User> findByGoogleId(String googleId);

    Optional<User> findByAppleId(String appleId);

    // 인증된 요청마다 매번 쓰지 않도록, 마지막 기록이 threshold보다 오래됐을 때만 갱신 (JwtAuthFilter용)
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastActiveAt = :now " +
           "WHERE u.id = :userId AND (u.lastActiveAt IS NULL OR u.lastActiveAt < :threshold)")
    void touchLastActiveIfStale(@Param("userId") UUID userId, @Param("now") LocalDateTime now, @Param("threshold") LocalDateTime threshold);

    List<User> findAllByOrderByLastActiveAtDesc();
}
