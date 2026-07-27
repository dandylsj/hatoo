package com.hatoo.domain.coupang;

public record CoupangProduct(
        Long productId,
        String productName,
        int productPrice,
        String productImage,
        String productUrl,
        boolean isRocket,
        boolean isFreeShipping
) {}
