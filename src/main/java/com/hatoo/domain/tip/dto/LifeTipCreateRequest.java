package com.hatoo.domain.tip.dto;

import com.hatoo.domain.tip.LifeTipCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LifeTipCreateRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String content,
        String imageUrl,
        @NotNull LifeTipCategory category
) {}
