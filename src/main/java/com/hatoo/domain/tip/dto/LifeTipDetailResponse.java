package com.hatoo.domain.tip.dto;

import com.hatoo.domain.tip.LifeTip;
import com.hatoo.domain.tip.LifeTipCategory;

import java.time.LocalDateTime;
import java.util.UUID;

public record LifeTipDetailResponse(
        UUID id,
        String title,
        String content,
        String imageUrl,
        LifeTipCategory category,
        String categoryDisplayName,
        int viewCount,
        int bookmarkCount,
        boolean bookmarked,
        LocalDateTime createdAt
) {
    public static LifeTipDetailResponse of(LifeTip tip, int bookmarkCount, boolean bookmarked) {
        return new LifeTipDetailResponse(
                tip.getId(),
                tip.getTitle(),
                tip.getContent(),
                tip.getImageUrl(),
                tip.getCategory(),
                tip.getCategory().getDisplayName(),
                tip.getViewCount(),
                bookmarkCount,
                bookmarked,
                tip.getCreatedAt()
        );
    }
}
