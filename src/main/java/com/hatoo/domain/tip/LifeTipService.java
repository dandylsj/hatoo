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

import java.util.List;
import java.util.UUID;

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

    // 목록 조회 (카테고리 필터)
    @Transactional(readOnly = true)
    public List<LifeTipListResponse> getList(String token, LifeTipCategory category) {
        UUID userId = extractUserIdSafe(token);
        List<LifeTip> tips = (category == null)
                ? lifeTipRepository.findAllByOrderByCreatedAtDesc()
                : lifeTipRepository.findByCategoryOrderByCreatedAtDesc(category);

        List<UUID> myBookmarkedTipIds = (userId != null)
                ? tipBookmarkRepository.findTipIdsByUserId(userId)
                : List.of();

        return tips.stream()
                .map(tip -> LifeTipListResponse.of(
                        tip,
                        tipBookmarkRepository.countById_TipId(tip.getId()),
                        myBookmarkedTipIds.contains(tip.getId())
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

        boolean bookmarked = (userId != null) &&
                tipBookmarkRepository.existsById(new TipBookmarkId(userId, tipId));
        int bookmarkCount = tipBookmarkRepository.countById_TipId(tipId);
        return LifeTipDetailResponse.of(tip, bookmarkCount, bookmarked);
    }

    // 북마크 토글
    @Transactional
    public boolean toggleBookmark(String token, UUID tipId) {
        UUID userId = jwtUtil.extractUserId(token);
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
        LifeTip tip = LifeTip.create(req.title(), req.content(), req.imageUrl(), req.category());
        lifeTipRepository.save(tip);
        return LifeTipDetailResponse.of(tip, 0, false);
    }

    @Transactional
    public LifeTipDetailResponse update(UUID tipId, LifeTipCreateRequest req) {
        LifeTip tip = lifeTipRepository.findById(tipId)
                .orElseThrow(() -> new CustomException(ErrorMessage.TIP_NOT_FOUND));
        tip.update(req.title(), req.content(), req.imageUrl(), req.category());
        int bookmarkCount = tipBookmarkRepository.countById_TipId(tipId);
        return LifeTipDetailResponse.of(tip, bookmarkCount, false);
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

        return bookmarkedIds.stream()
                .map(tipId -> lifeTipRepository.findById(tipId).orElse(null))
                .filter(tip -> tip != null)
                .map(tip -> LifeTipListResponse.of(
                        tip,
                        tipBookmarkRepository.countById_TipId(tip.getId()),
                        true
                ))
                .toList();
    }
}
