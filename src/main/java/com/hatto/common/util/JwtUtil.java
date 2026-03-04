package com.hatto.common.util;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.security.SignatureException;
import java.util.Date;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTHORIZATION = "Authorization";
    
    // 토큰 만료 시간
    public static final long TOKEN_TIME = 30 * 60 * 1000L;  // 30분
    public static final long REFRESH_TOKEN_TIME = 7 * 24 * 60 * 60 * 1000L; // 7일

    @Value("${jwt.secret}")
    private String jwtSecretKey;
    private SecretKey secretKey;
    private JwtParser jwtParser;

    @PostConstruct
    public void init() {
        byte[] bytes = Decoders.BASE64.decode(jwtSecretKey);
        this.secretKey = Keys.hmacShaKeyFor(bytes);
        this.jwtParser = Jwts.parser().verifyWith(secretKey).build();
    }

    // Access Token 생성
    public String generateToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("userRole", userRole)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + TOKEN_TIME))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // Refresh Token 생성
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim(AUTHORIZATION, userRole.name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + REFRESH_TOKEN_TIME))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        if (StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX)) {
            token = token.substring(7);
        }

        try {
            jwtParser.parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            log.error("유효하지 않는 JWT 입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.error("잘못된 JWT 토큰 입니다.");
        }
        return false;
    }

    // Claims 추출
    public Claims extractAllClaims(String token) {
        if (token.startsWith(BEARER_PREFIX)) {
            token = token.substring(7);
        }
        return jwtParser.parseSignedClaims(token).getPayload();
    }

    // ID 추출
    public Long extractUserId(String token) {
        if (token.startsWith(BEARER_PREFIX)) token = token.substring(7);
        return Long.valueOf(extractAllClaims(token).getSubject());
    }


}
