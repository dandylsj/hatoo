package com.hatoo.domain.tip;

import com.hatoo.common.BaseEntity;
import com.hatoo.common.StringListConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
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

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> imageUrls = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LifeTipCategory category;

    @Column(nullable = false)
    private int viewCount = 0;

    public static LifeTip create(String title, String content, List<String> imageUrls, LifeTipCategory category) {
        LifeTip tip = new LifeTip();
        tip.title = title;
        tip.content = content;
        tip.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        tip.category = category;
        return tip;
    }

    public void update(String title, String content, List<String> imageUrls, LifeTipCategory category) {
        this.title = title;
        this.content = content;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.category = category;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }
}
