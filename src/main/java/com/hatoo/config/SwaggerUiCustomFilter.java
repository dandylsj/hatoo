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

    // Swagger UI는 React 렌더링이므로 DOMContentLoaded 이후 MutationObserver로 처리
    private static final String CUSTOM_SCRIPT = """
            <script>
            (function() {
                function hideCollapseToggles() {
                    // "^ Collapse all" 버튼 숨기기
                    document.querySelectorAll('.model-toggle').forEach(function(el) {
                        el.style.setProperty('display', 'none', 'important');
                    });
                    // 속성 설명/예시 항상 펼침
                    document.querySelectorAll('.inner-object').forEach(function(el) {
                        el.style.setProperty('display', 'block', 'important');
                    });
                }
                const observer = new MutationObserver(hideCollapseToggles);
                document.addEventListener('DOMContentLoaded', function() {
                    observer.observe(document.body, { childList: true, subtree: true });
                    hideCollapseToggles();
                });
            })();
            </script>
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

        if (html.contains("</body>")) {
            html = html.replace("</body>", CUSTOM_SCRIPT + "</body>");
        }

        byte[] modified = html.getBytes(StandardCharsets.UTF_8);
        response.setContentLength(modified.length);
        response.getOutputStream().write(modified);
    }
}
