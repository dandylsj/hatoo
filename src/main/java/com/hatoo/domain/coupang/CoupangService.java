package com.hatoo.domain.coupang;

import com.hatoo.domain.tip.LifeTipCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class CoupangService {

    private static final String BASE_URL = "https://api-gateway.coupang.com";
    private static final String SEARCH_PATH = "/v2/providers/affiliate_open_api/apis/openapi/products/search";
    private static final int PRODUCT_LIMIT = 5;
    private static final long CACHE_TTL_MS = 6 * 60 * 60 * 1000L; // 6시간

    @Value("${coupang.access-key}")
    private String accessKey;

    @Value("${coupang.secret-key}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, CachedResult> cache = new ConcurrentHashMap<>();

    public List<CoupangProduct> getProductsByCategory(LifeTipCategory category) {
        String keyword = toKeyword(category);
        CachedResult cached = cache.get(keyword);
        if (cached != null && !cached.isExpired()) {
            log.info("[Coupang] 캐시 사용 - keyword: {}", keyword);
            return cached.products();
        }
        List<CoupangProduct> products = fetchProducts(keyword);
        if (!products.isEmpty()) {
            cache.put(keyword, new CachedResult(products, System.currentTimeMillis()));
        }
        return products;
    }

    private String toKeyword(LifeTipCategory category) {
        return switch (category) {
            case KITCHEN -> "주방청소용품";
            case BATHROOM -> "욕실청소용품";
            case LAUNDRY -> "세탁세제";
            case RECYCLING -> "분리수거함";
        };
    }

    @SuppressWarnings("unchecked")
    private List<CoupangProduct> fetchProducts(String keyword) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String datetime = sdf.format(new Date());

            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String queryString = "keyword=" + encodedKeyword + "&limit=" + PRODUCT_LIMIT + "&subId=hatoo";
            String message = datetime + "GET" + SEARCH_PATH + "?" + queryString;
            String signature = hmacSha256(secretKey, message);

            log.info("[Coupang] 요청 - datetime: {}, message: {}", datetime, message);

            String authorization = "CEA algorithm=HmacSHA256, access-id=" + accessKey
                    + ", signed-date=" + datetime + ", signature=" + signature;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorization);
            headers.set("Content-Type", "application/json;charset=UTF-8");

            String url = BASE_URL + SEARCH_PATH + "?" + queryString;
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

            Map<?, ?> body = response.getBody();
            log.info("[Coupang] API 응답: {}", body);
            if (body == null || !"00".equals(body.get("rCode"))) {
                log.warn("[Coupang] API 응답 오류 - rCode: {}, message: {}", body != null ? body.get("rCode") : "null", body != null ? body.get("rMessage") : "null");
                return List.of();
            }

            Map<?, ?> data = (Map<?, ?>) body.get("data");
            List<?> productData = (List<?>) data.get("productData");
            if (productData == null) return List.of();

            return productData.stream()
                    .map(p -> {
                        Map<?, ?> m = (Map<?, ?>) p;
                        return new CoupangProduct(
                                toLong(m.get("productId")),
                                (String) m.get("productName"),
                                toInt(m.get("productPrice")),
                                (String) m.get("productImage"),
                                (String) m.get("productUrl"),
                                Boolean.TRUE.equals(m.get("isRocket")),
                                Boolean.TRUE.equals(m.get("isFreeShipping"))
                        );
                    })
                    .toList();

        } catch (Exception e) {
            log.error("[Coupang] API 호출 실패: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private String hmacSha256(String secret, String message) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
        byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : rawHmac) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.longValue();
        return Long.parseLong(val.toString());
    }

    private int toInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number n) return n.intValue();
        return Integer.parseInt(val.toString());
    }

    private record CachedResult(List<CoupangProduct> products, long cachedAt) {
        boolean isExpired() {
            return System.currentTimeMillis() - cachedAt > CACHE_TTL_MS;
        }
    }
}
