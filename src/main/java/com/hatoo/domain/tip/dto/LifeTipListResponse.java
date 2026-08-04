package com.hatoo.domain.tip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hatoo.domain.tip.LifeTip;
import com.hatoo.domain.tip.LifeTipCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "꿀팁 목록 조회 응답")
public record LifeTipListResponse(
        @Schema(description = "꿀팁 ID")
        UUID id,

        @Schema(description = "제목")
        String title,

        @Schema(description = "본문 요약 (60자 초과 시 말줄임)")
        String summary,

        @Schema(description = "본문에 첨부된 이미지 URL 목록")
        List<String> imageUrls,

        @Schema(description = "카테고리")
        LifeTipCategory category,

        @Schema(description = "카테고리 화면 표시명")
        String categoryDisplayName,

        @Schema(description = "조회수")
        int viewCount,

        @Schema(description = "북마크(저장) 수")
        int bookmarkCount,

        @Schema(description = "요청한 사용자의 북마크 여부")
        @JsonProperty("isBookMarked") boolean isBookMarked,

        @Schema(description = "HOT 태그 노출 여부")
        @JsonProperty("isHotTag") boolean isHotTag,

        @Schema(description = "등록일시")
        LocalDateTime createdAt
) {
    public static LifeTipListResponse of(LifeTip tip, int bookmarkCount, boolean isBookMarked, boolean isHotTag) {
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
                isBookMarked,
                isHotTag,
                tip.getCreatedAt()
        );
    }
}
