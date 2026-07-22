package com.hatoo.domain.tip;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tip_bookmark")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TipBookmark {

    @EmbeddedId
    private TipBookmarkId id;

    public TipBookmark(TipBookmarkId id) {
        this.id = id;
    }
}
