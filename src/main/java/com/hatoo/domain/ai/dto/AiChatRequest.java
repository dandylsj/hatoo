package com.hatoo.domain.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiChatRequest(
        @NotBlank(message = "질문을 입력해주세요.")
        @Size(max = 500, message = "질문은 500자 이내로 입력해주세요.")
        String message
) {}
