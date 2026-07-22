package com.hatoo.domain.tip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TipBookmarkRepository extends JpaRepository<TipBookmark, TipBookmarkId> {
    boolean existsById(TipBookmarkId id);

    @Query("SELECT tb.id.tipId FROM TipBookmark tb WHERE tb.id.userId = :userId")
    List<UUID> findTipIdsByUserId(UUID userId);

    int countById_TipId(UUID tipId);
}
