package com.hatoo.domain.ai.dto;

import com.hatoo.domain.ai.AiChatHistory;

import java.time.LocalDateTime;
import java.util.UUID;

public record AiChatHistoryResponse(
        UUID id,
        String userMessage,
        String aiResponse,
        int tokensUsed,
        LocalDateTime createdAt
) {
    public static AiChatHistoryResponse from(AiChatHistory h) {
        return new AiChatHistoryResponse(
                h.getId(),
                h.getUserMessage(),
                h.getAiResponse(),
                h.getTokensUsed(),
                h.getCreatedAt()
        );
    }
}
