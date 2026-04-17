package com.hatoo.common.util;


import com.hatoo.common.exception.CustomException;
import com.hatoo.common.exception.ErrorMessage;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {

    public static final String BEARER_PREFIX = "Bearer ";
    public static final long ACCESS_TOKEN_TIME = 30 * 1000L; // 30초
    public static final long REFRESH_TOKEN_TIME = 5 * 60 * 1000L; // 5분

    @Value("${JWT_SECRET_KEY}")
    private String secretKeyString;

    private SecretKey key;
    private JwtParser parser;

    @PostConstruct
    public void init() {

        byte[] bytes = Decoders.BASE64.decode(secretKeyString);
        this.key = Keys.hmacShaKeyFor(bytes);
        this.parser = Jwts.parser()
                .verifyWith(this.key)
                .build();
    }

    public String generateAccessToken(String loginId, String nickName) {

        Date now = new Date();
        return Jwts.builder()
                .subject(loginId)
                .claim("nickName", nickName)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ACCESS_TOKEN_TIME))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(UUID userId) {

        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + REFRESH_TOKEN_TIME))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public void validateToken(String token) {
        try {
            parser.parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            log.error("[JWT] 토큰 만료 - 만료시각: {}", e.getClaims().getExpiration());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("[JWT] 잘못된 토큰 형식 - token prefix: {}", token.length() > 20 ? token.substring(0, 20) + "..." : token);
            throw e;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("[JWT] 서명 검증 실패");
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("[JWT] 지원하지 않는 JWT - 메시지: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("[JWT] JWT 클레임이 비어있거나 null - 메시지: {}", e.getMessage());
            throw e;
        }
    }



    private Claims extractAllClaims(String token) {
        return parser.parseSignedClaims(token).getPayload();
    }

    public String extractLoginId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).getSubject());
    }

    public String extractUserEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public String extractName(String token) {
        return extractAllClaims(token).get("name", String.class);
    }

    // 리프레시 토큰 전용 검증 (만료/위변조 → CustomException으로 변환)
    public void validateRefreshToken(String token) {
        try {
            parser.parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            log.error("[JWT] 리프레시 토큰 만료 - 만료시각: {}", e.getClaims().getExpiration());
            throw new CustomException(ErrorMessage.EXPIRED_TOKEN);
        } catch (Exception e) {
            log.error("[JWT] 리프레시 토큰 검증 실패 - {}", e.getMessage());
            throw new CustomException(ErrorMessage.INVALID_REFRESH_TOKEN);
        }
    }
}