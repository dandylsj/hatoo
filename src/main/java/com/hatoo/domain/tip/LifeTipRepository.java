package com.hatoo.domain.tip;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LifeTipRepository extends JpaRepository<LifeTip, UUID> {
    List<LifeTip> findAllByOrderByCreatedAtDesc();
    List<LifeTip> findByCategoryOrderByCreatedAtDesc(LifeTipCategory category);
}
