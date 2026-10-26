package com.hatoo.config;

import com.hatoo.common.util.JwtUtil;
import com.hatoo.domain.user.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    // 요청마다 매번 DB에 쓰지 않도록, 마지막 활동 기록 후 이 시간 안에는 갱신을 건너뜀
    private static final java.time.Duration ACTIVITY_UPDATE_INTERVAL = java.time.Duration.ofMinutes(5);

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            try {
                jwtUtil.validateToken(token);
                String loginId = jwtUtil.extractLoginId(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(loginId, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                touchLastActive(token);

            } catch (JwtException | IllegalArgumentException e) {
                log.warn("[JwtAuthFilter] 유효하지 않은 토큰: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                // 인증 실패 처리 - Security가 401 반환
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"statusCode\": 401, \"message\": \"유효하지 않은 토큰입니다.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (!StringUtils.hasText(bearerToken)) {
            return null;
        }
        if (bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        // Bearer 접두사 없이 토큰만 보낸 경우도 허용
        return bearerToken;
    }

    // 인증된 요청이 올 때마다 lastActiveAt을 갱신하되, 최근에 이미 갱신했다면 DB 쓰기를 건너뛴다.
    // 실패해도 본 요청 처리에는 영향 없도록 예외를 삼키고 경고만 남긴다.
    private void touchLastActive(String token) {
        try {
            UUID userId = jwtUtil.extractUserId(token);
            LocalDateTime now = LocalDateTime.now(KST);
            userRepository.touchLastActiveIfStale(userId, now, now.minus(ACTIVITY_UPDATE_INTERVAL));
        } catch (Exception e) {
            log.warn("[JwtAuthFilter] lastActiveAt 갱신 실패: {}", e.getMessage());
        }
    }
}
