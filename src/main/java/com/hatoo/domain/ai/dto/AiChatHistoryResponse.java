package com.hatoo.domain.ai.dto;

import com.hatoo.domain.ai.AiChatHistory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "AI 챗봇 대화 이력")
public record AiChatHistoryResponse(
        @Schema(description = "대화 이력 ID")
        UUID id,

        @Schema(description = "사용자 질문", example = "화장실 곰팡이 어떻게 없애요?")
        String userMessage,

        @Schema(description = "AI 답변", example = "락스와 물을 1:10 비율로 희석해서 뿌려주세요...")
        String aiResponse,

        @Schema(description = "답변 생성에 사용된 토큰 수", example = "256")
        int tokensUsed,

        @Schema(description = "대화 일시")
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
