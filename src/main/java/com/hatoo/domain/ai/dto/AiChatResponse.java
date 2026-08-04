package com.hatoo.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 챗봇 답변 응답")
public record AiChatResponse(
        @Schema(description = "AI 답변 내용")
        String answer
) {}
