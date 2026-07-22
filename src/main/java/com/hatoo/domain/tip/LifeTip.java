package com.hatoo.domain.tip;

import com.hatoo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "life_tip")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LifeTip extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LifeTipCategory category;

    @Column(nullable = false)
    private int viewCount = 0;

    public void incrementViewCount() {
        this.viewCount++;
    }
}
