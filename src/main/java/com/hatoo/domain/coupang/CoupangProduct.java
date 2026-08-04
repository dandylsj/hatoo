package com.hatoo.domain.coupang;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "쿠팡 파트너스 광고 상품")
public record CoupangProduct(
        @Schema(description = "쿠팡 상품 ID")
        Long productId,

        @Schema(description = "상품명")
        String productName,

        @Schema(description = "상품 가격(원)")
        int productPrice,

        @Schema(description = "상품 이미지 URL")
        String productImage,

        @Schema(description = "상품 상세 페이지 URL (쿠팡 파트너스 트래킹 링크 포함)")
        String productUrl,

        @Schema(description = "로켓배송 상품 여부")
        boolean isRocket,

        @Schema(description = "무료배송 상품 여부")
        boolean isFreeShipping
) {}
