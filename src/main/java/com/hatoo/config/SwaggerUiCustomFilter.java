package com.hatoo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Order(1)
public class SwaggerUiCustomFilter extends OncePerRequestFilter {

    private static final String CUSTOM_CSS = """
            <style>
            /* 개별 속성의 접기/펼치기 토글 화살표 숨기기 */
            span.model > span.model-toggle,
            .model-toggle { display: none !important; }

            /* 속성 설명·예시 항상 표시 */
            span.model > .inner-object,
            .inner-object { display: block !important; visibility: visible !important; }

            /* 'Collapse all' 텍스트도 포함된 헤더 토글 숨기기 */
            .model-collapse-toggle { display: none !important; }
            </style>
            """;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (!uri.endsWith("/swagger-ui/index.html")) {
            chain.doFilter(request, response);
            return;
        }

        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        chain.doFilter(request, wrapper);

        byte[] original = wrapper.getContentAsByteArray();
        String html = new String(original, StandardCharsets.UTF_8);

        if (html.contains("</head>")) {
            html = html.replace("</head>", CUSTOM_CSS + "</head>");
        }

        byte[] modified = html.getBytes(StandardCharsets.UTF_8);
        response.setContentLength(modified.length);
        response.getOutputStream().write(modified);
    }
}
