package com.hatoo.domain.tip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hatoo.domain.tip.LifeTip;
import com.hatoo.domain.tip.LifeTipCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record LifeTipDetailResponse(
        UUID id,
        String title,
        String content,
        List<String> imageUrls,
        LifeTipCategory category,
        String categoryDisplayName,
        int viewCount,
        int bookmarkCount,
        @JsonProperty("isBookMarked") boolean isBookMarked,
        @JsonProperty("isHotTag") boolean isHotTag,
        LocalDateTime createdAt
) {
    public static LifeTipDetailResponse of(LifeTip tip, int bookmarkCount, boolean isBookMarked, boolean isHotTag) {
        return new LifeTipDetailResponse(
                tip.getId(),
                tip.getTitle(),
                tip.getContent(),
                tip.getImageUrls(),
                tip.getCategory(),
                tip.getCategory().getDisplayName(),
                tip.getViewCount(),
                bookmarkCount,
                isBookMarked,
                isHotTag,
                tip.getCreatedAt()
        );
    }
}
