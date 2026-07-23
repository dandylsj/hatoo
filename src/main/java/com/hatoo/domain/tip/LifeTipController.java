package com.hatoo.domain.tip;

import com.hatoo.common.minio.MinioService;
import com.hatoo.domain.tip.dto.LifeTipCreateRequest;
import com.hatoo.domain.tip.dto.LifeTipDetailResponse;
import com.hatoo.domain.tip.dto.LifeTipListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "LifeTip", description = "생활 꿀팁 API")
@RestController
@RequestMapping("/tips")
@RequiredArgsConstructor
public class LifeTipController {

    private final LifeTipService lifeTipService;
    private final MinioService minioService;

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

    // ── 관리자용 (Swagger로 직접 데이터 입력) ──

    @Operation(summary = "[관리자] 이미지 업로드", description = "여러 이미지를 한번에 업로드하고 URL 목록을 반환합니다. 반환된 imageUrls를 꿀팁 등록 시 사용하세요.")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, List<String>>> uploadImages(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @RequestParam("files") List<MultipartFile> files) {
        List<String> imageUrls = files.stream()
                .map(minioService::uploadImage)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("imageUrls", imageUrls));
    }

    @Operation(summary = "[관리자] 꿀팁 등록", description = "imageUrl: MinIO에 올린 이미지 URL 입력. category: KITCHEN / BATHROOM / LAUNDRY / RECYCLING")
    @PostMapping
    public ResponseEntity<LifeTipDetailResponse> create(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @Valid @RequestBody LifeTipCreateRequest request) {
        return ResponseEntity.ok(lifeTipService.create(request));
    }

    @Operation(summary = "[관리자] 꿀팁 수정")
    @PutMapping("/{tipId}")
    public ResponseEntity<LifeTipDetailResponse> update(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID tipId,
            @Valid @RequestBody LifeTipCreateRequest request) {
        return ResponseEntity.ok(lifeTipService.update(tipId, request));
    }

    @Operation(summary = "[관리자] 꿀팁 삭제")
    @DeleteMapping("/{tipId}")
    public ResponseEntity<Void> delete(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID tipId) {
        lifeTipService.delete(tipId);
        return ResponseEntity.noContent().build();
    }
}
