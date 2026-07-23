package com.hatoo.domain.tip.dto;

import com.hatoo.domain.tip.LifeTip;
import com.hatoo.domain.tip.LifeTipCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record LifeTipListResponse(
        UUID id,
        String title,
        String summary,
        List<String> imageUrls,
        LifeTipCategory category,
        String categoryDisplayName,
        int viewCount,
        int bookmarkCount,
        boolean bookmarked,
        LocalDateTime createdAt
) {
    public static LifeTipListResponse of(LifeTip tip, int bookmarkCount, boolean bookmarked) {
        String summary = tip.getContent().length() > 60
                ? tip.getContent().substring(0, 60) + "..."
                : tip.getContent();
        return new LifeTipListResponse(
                tip.getId(),
                tip.getTitle(),
                summary,
                tip.getImageUrls(),
                tip.getCategory(),
                tip.getCategory().getDisplayName(),
                tip.getViewCount(),
                bookmarkCount,
                bookmarked,
                tip.getCreatedAt()
        );
    }
}
