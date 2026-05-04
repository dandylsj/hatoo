package com.hatoo.domain.alarmUserAgree;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlarmUserAgreeRepository extends JpaRepository<AlarmUserAgree, UUID> {

    Optional<AlarmUserAgree> findByUserId(UUID userId);
}
