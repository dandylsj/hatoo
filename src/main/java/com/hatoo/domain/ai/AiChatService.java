package com.hatoo.domain.ai;

import com.hatoo.common.util.JwtUtil;
import com.hatoo.domain.ai.dto.AiChatHistoryResponse;
import com.hatoo.domain.ai.dto.AiChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    private static final String SYSTEM_PROMPT =
            "당신은 집안일 전문 도우미 '하투'입니다. " +
            "청소, 정리정돈, 요리, 세탁, 생활 꿀팁 등 집안일에 관한 질문에 친절하고 실용적으로 한국어로 답변해주세요. " +
            "답변은 간결하게 핵심만 담아주세요. " +
            "집안일과 전혀 관련 없는 질문에는 '저는 집안일 관련 질문만 답변할 수 있어요 😊'라고만 답해주세요.";

    @Value("${gemini.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final AiChatHistoryRepository aiChatHistoryRepository;
    private final JwtUtil jwtUtil;

    public AiChatResponse chat(String token, String message) {
        UUID userId = jwtUtil.extractUserId(token);
        String url = GEMINI_API_URL + apiKey;

        Map<String, Object> requestBody = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", SYSTEM_PROMPT))
                ),
                "contents", List.of(
                        Map.of("role", "user", "parts", List.of(Map.of("text", message)))
                ),
                "generationConfig", Map.of("maxOutputTokens", 800)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url, new HttpEntity<>(requestBody, headers), Map.class);

            Map<?, ?> body = response.getBody();

            // 답변 파싱
            List<?> candidates = (List<?>) body.get("candidates");
            Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content = (Map<?, ?>) candidate.get("content");
            List<?> parts = (List<?>) content.get("parts");
            String answer = (String) ((Map<?, ?>) parts.get(0)).get("text");

            // 토큰 사용량 파싱
            int tokensUsed = 0;
            Map<?, ?> usageMeta = (Map<?, ?>) body.get("usageMetadata");
            if (usageMeta != null && usageMeta.get("totalTokenCount") != null) {
                tokensUsed = ((Number) usageMeta.get("totalTokenCount")).intValue();
            }

            aiChatHistoryRepository.save(new AiChatHistory(userId, message, answer, tokensUsed));
            log.info("[AI] 대화 저장 완료 - userId: {}, tokensUsed: {}", userId, tokensUsed);

            return new AiChatResponse(answer);
        } catch (Exception e) {
            log.error("[AI] Gemini API 오류: {}", e.getMessage());
            return new AiChatResponse("죄송해요, 지금은 답변하기 어려워요. 잠시 후 다시 시도해주세요 😢");
        }
    }

    public List<AiChatHistoryResponse> getHistory(String token) {
        UUID userId = jwtUtil.extractUserId(token);
        return aiChatHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(AiChatHistoryResponse::from)
                .toList();
    }
}
