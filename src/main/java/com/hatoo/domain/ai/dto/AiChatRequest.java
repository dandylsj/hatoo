package com.hatoo.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "AI 챗봇 질문 요청")
public record AiChatRequest(
        @Schema(description = "사용자 질문 (500자 이내)")
        @NotBlank(message = "질문을 입력해주세요.")
        @Size(max = 500, message = "질문은 500자 이내로 입력해주세요.")
        String message
) {}
