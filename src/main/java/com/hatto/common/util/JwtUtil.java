package com.hatto.common.util;


import com.hatto.common.exception.CustomException;
import com.hatto.common.exception.ErrorMessage;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.security.SignatureException;
import java.util.Date;

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
        return BEARER_PREFIX + Jwts.builder()
                .subject(loginId)
                .claim("nickName", nickName)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ACCESS_TOKEN_TIME))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId) {

        Date now = new Date();
        return BEARER_PREFIX + Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + REFRESH_TOKEN_TIME))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public void validateToken(String token) {

        try {
            parser.parseSignedClaims(token);
        } catch (ExpiredJwtException expired) {
            throw new CustomException(ErrorMessage.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorMessage.INVALID_TOKEN);
        }
    }

    private Claims extractAllClaims(String token) {
        return parser.parseSignedClaims(token).getPayload();
    }

    public Long extractUserId(String token) {
        return Long.parseLong(extractAllClaims(token).getSubject());
    }

    public String extractUserEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public String extractName(String token) {
        return extractAllClaims(token).get("name", String.class);
    }

}