package com.hatoo.domain.tip;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LifeTipCategory {
    KITCHEN("주방"),
    BATHROOM("욕실"),
    LAUNDRY("세탁"),
    RECYCLING("분리수거");

    private final String displayName;
}
