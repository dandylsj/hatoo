package com.hatoo.domain.tip.dto;

import com.hatoo.domain.tip.LifeTipCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "꿀팁 등록 요청")
public record LifeTipCreateRequest(
        @Schema(description = "제목")
        @NotBlank @Size(max = 200) String title,

        @Schema(description = "본문 내용")
        @NotBlank String content,

        @Schema(description = "본문에 첨부된 이미지 URL 목록")
        List<String> imageUrls,

        @Schema(description = "카테고리 (KITCHEN, BATHROOM, LAUNDRY, RECYCLING)")
        @NotNull LifeTipCategory category
) {}
