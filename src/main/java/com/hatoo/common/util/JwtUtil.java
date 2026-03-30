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
    public static final long ACCESS_TOKEN_TIME = 24 * 60 * 60 * 1000L;
    public static final long REFRESH_TOKEN_TIME = 14 * 24 * 60 * 60 * 1000L;

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
        parser.parseSignedClaims(token);
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

}
