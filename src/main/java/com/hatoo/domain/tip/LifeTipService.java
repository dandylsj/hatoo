package com.hatoo.domain.tip;

import com.hatoo.common.exception.CustomException;
import com.hatoo.common.exception.ErrorMessage;
import com.hatoo.common.util.JwtUtil;
import com.hatoo.domain.tip.dto.LifeTipCreateRequest;
import com.hatoo.domain.tip.dto.LifeTipDetailResponse;
import com.hatoo.domain.tip.dto.LifeTipListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LifeTipService {

    private final LifeTipRepository lifeTipRepository;
    private final TipBookmarkRepository tipBookmarkRepository;
    private final JwtUtil jwtUtil;

    private UUID extractUserIdSafe(String token) {
        try {
            return jwtUtil.extractUserId(token);
        } catch (Exception e) {
            return null;
        }
    }

    private Map<UUID, Integer> buildBookmarkCountMap() {
        return tipBookmarkRepository.findAllTipBookmarkCounts().stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> ((Number) row[1]).intValue()
                ));
    }

    private Set<UUID> computeHotTipIds(List<LifeTip> allTips, Map<UUID, Integer> bookmarkCountMap) {
        return allTips.stream()
                .sorted((a, b) -> {
                    int scoreA = a.getViewCount() + bookmarkCountMap.getOrDefault(a.getId(), 0);
                    int scoreB = b.getViewCount() + bookmarkCountMap.getOrDefault(b.getId(), 0);
                    return scoreB - scoreA;
                })
                .limit(3)
                .map(LifeTip::getId)
                .collect(Collectors.toSet());
    }

    // 목록 조회 (카테고리 필터)
    @Transactional(readOnly = true)
    public List<LifeTipListResponse> getList(String token, LifeTipCategory category) {
        UUID userId = extractUserIdSafe(token);

        List<LifeTip> allTips = lifeTipRepository.findAllByOrderByCreatedAtDesc();
        Map<UUID, Integer> bookmarkCountMap = buildBookmarkCountMap();
        Set<UUID> hotTipIds = computeHotTipIds(allTips, bookmarkCountMap);

        List<LifeTip> tips = (category == null) ? allTips
                : allTips.stream().filter(t -> t.getCategory() == category).collect(Collectors.toList());

        List<UUID> myBookmarkedTipIds = (userId != null)
                ? tipBookmarkRepository.findTipIdsByUserId(userId)
                : List.of();

        return tips.stream()
                .map(tip -> LifeTipListResponse.of(
                        tip,
                        bookmarkCountMap.getOrDefault(tip.getId(), 0),
                        myBookmarkedTipIds.contains(tip.getId()),
                        hotTipIds.contains(tip.getId())
                ))
                .toList();
    }

    // 상세 조회 (조회수 증가)
    @Transactional
    public LifeTipDetailResponse getDetail(String token, UUID tipId) {
        UUID userId = extractUserIdSafe(token);
        LifeTip tip = lifeTipRepository.findById(tipId)
                .orElseThrow(() -> new CustomException(ErrorMessage.TIP_NOT_FOUND));
        tip.incrementViewCount();

        Map<UUID, Integer> bookmarkCountMap = buildBookmarkCountMap();
        List<LifeTip> allTips = lifeTipRepository.findAllByOrderByCreatedAtDesc();
        Set<UUID> hotTipIds = computeHotTipIds(allTips, bookmarkCountMap);

        boolean bookmarked = (userId != null) &&
                tipBookmarkRepository.existsById(new TipBookmarkId(userId, tipId));
        int bookmarkCount = bookmarkCountMap.getOrDefault(tipId, 0);
        boolean hot = hotTipIds.contains(tipId);

        return LifeTipDetailResponse.of(tip, bookmarkCount, bookmarked, hot);
    }

    // 북마크 토글
    @Transactional
    public boolean toggleBookmark(String token, UUID tipId) {
        UUID userId = extractUserIdSafe(token);
        if (userId == null) throw new CustomException(ErrorMessage.ACCESS_DENIED);
        lifeTipRepository.findById(tipId)
                .orElseThrow(() -> new CustomException(ErrorMessage.TIP_NOT_FOUND));

        TipBookmarkId bookmarkId = new TipBookmarkId(userId, tipId);
        if (tipBookmarkRepository.existsById(bookmarkId)) {
            tipBookmarkRepository.deleteById(bookmarkId);
            return false;
        } else {
            tipBookmarkRepository.save(new TipBookmark(bookmarkId));
            return true;
        }
    }

    // ── 관리자용 ──

    @Transactional
    public LifeTipDetailResponse create(LifeTipCreateRequest req) {
        LifeTip tip = LifeTip.create(req.title(), req.content(), req.imageUrls(), req.category());
        lifeTipRepository.save(tip);
        return LifeTipDetailResponse.of(tip, 0, false, false);
    }

    @Transactional
    public LifeTipDetailResponse update(UUID tipId, LifeTipCreateRequest req) {
        LifeTip tip = lifeTipRepository.findById(tipId)
                .orElseThrow(() -> new CustomException(ErrorMessage.TIP_NOT_FOUND));
        tip.update(req.title(), req.content(), req.imageUrls(), req.category());
        int bookmarkCount = tipBookmarkRepository.countById_TipId(tipId);
        return LifeTipDetailResponse.of(tip, bookmarkCount, false, false);
    }

    @Transactional
    public void delete(UUID tipId) {
        LifeTip tip = lifeTipRepository.findById(tipId)
                .orElseThrow(() -> new CustomException(ErrorMessage.TIP_NOT_FOUND));
        tipBookmarkRepository.deleteAll(
                tipBookmarkRepository.findAll().stream()
                        .filter(b -> b.getId().getTipId().equals(tipId))
                        .toList()
        );
        lifeTipRepository.delete(tip);
    }

    // 내가 저장한 꿀팁 목록
    @Transactional(readOnly = true)
    public List<LifeTipListResponse> getMyBookmarks(String token) {
        UUID userId = jwtUtil.extractUserId(token);
        List<UUID> bookmarkedIds = tipBookmarkRepository.findTipIdsByUserId(userId);
        Map<UUID, Integer> bookmarkCountMap = buildBookmarkCountMap();
        List<LifeTip> allTips = lifeTipRepository.findAllByOrderByCreatedAtDesc();
        Set<UUID> hotTipIds = computeHotTipIds(allTips, bookmarkCountMap);

        return bookmarkedIds.stream()
                .map(id -> lifeTipRepository.findById(id).orElse(null))
                .filter(tip -> tip != null)
                .map(tip -> LifeTipListResponse.of(
                        tip,
                        bookmarkCountMap.getOrDefault(tip.getId(), 0),
                        true,
                        hotTipIds.contains(tip.getId())
                ))
                .toList();
    }
}
