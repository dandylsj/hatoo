package com.hatoo.domain.tip;

import com.hatoo.domain.tip.dto.LifeTipDetailResponse;
import com.hatoo.domain.tip.dto.LifeTipListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "LifeTip", description = "생활 꿀팁 API")
@RestController
@RequestMapping("/tips")
@RequiredArgsConstructor
public class LifeTipController {

    private final LifeTipService lifeTipService;

    @Operation(summary = "꿀팁 목록 조회", description = "카테고리 필터 없이 전체 조회 가능. category: KITCHEN, BATHROOM, LAUNDRY, RECYCLING")
    @GetMapping
    public ResponseEntity<List<LifeTipListResponse>> getList(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @RequestParam(required = false) LifeTipCategory category) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(lifeTipService.getList(token, category));
    }

    @Operation(summary = "꿀팁 상세 조회", description = "조회 시 viewCount가 1 증가합니다.")
    @GetMapping("/{tipId}")
    public ResponseEntity<LifeTipDetailResponse> getDetail(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID tipId) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(lifeTipService.getDetail(token, tipId));
    }

    @Operation(summary = "북마크 토글", description = "저장 → true, 해제 → false 반환")
    @PostMapping("/{tipId}/bookmark")
    public ResponseEntity<Map<String, Boolean>> toggleBookmark(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID tipId) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        boolean bookmarked = lifeTipService.toggleBookmark(token, tipId);
        return ResponseEntity.ok(Map.of("bookmarked", bookmarked));
    }

    @Operation(summary = "내가 저장한 꿀팁 목록", description = "북마크한 꿀팁을 최신순으로 반환합니다.")
    @GetMapping("/bookmarks")
    public ResponseEntity<List<LifeTipListResponse>> getMyBookmarks(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(lifeTipService.getMyBookmarks(token));
    }
}
