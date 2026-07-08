package com.hatoo.domain.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AiChatHistoryRepository extends JpaRepository<AiChatHistory, UUID> {
    List<AiChatHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
