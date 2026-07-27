package com.hatoo.domain.coupang;

import com.hatoo.common.exception.CustomException;
import com.hatoo.common.exception.ErrorMessage;
import com.hatoo.domain.tip.LifeTip;
import com.hatoo.domain.tip.LifeTipRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Coupang", description = "쿠팡 파트너스 광고 상품 API")
@RestController
@RequestMapping("/tips")
@RequiredArgsConstructor
public class CoupangController {

    private final CoupangService coupangService;
    private final LifeTipRepository lifeTipRepository;

    @Operation(summary = "꿀팁 연관 쿠팡 광고 상품 조회",
            description = "해당 꿀팁의 카테고리에 맞는 쿠팡 파트너스 상품을 반환합니다. 6시간 캐시 적용.")
    @GetMapping("/{tipId}/sponsored-products")
    public ResponseEntity<List<CoupangProduct>> getSponsoredProducts(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID tipId) {
        LifeTip tip = lifeTipRepository.findById(tipId)
                .orElseThrow(() -> new CustomException(ErrorMessage.TIP_NOT_FOUND));
        List<CoupangProduct> products = coupangService.getProductsByCategory(tip.getCategory());
        return ResponseEntity.ok(products);
    }
}
