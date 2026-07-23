package com.hatoo.domain.ai;

import com.hatoo.domain.ai.dto.AiChatHistoryResponse;
import com.hatoo.domain.ai.dto.AiChatRequest;
import com.hatoo.domain.ai.dto.AiChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AI", description = "AI 집안일 도우미 API")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @Operation(summary = "AI에게 집안일 질문하기", description = "집안일 관련 질문을 입력하면 AI 도우미 '하투'가 답변합니다. 대화 내역은 DB에 저장됩니다.")
    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @Valid @RequestBody AiChatRequest request) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(aiChatService.chat(token, request.message()));
    }

    @Operation(summary = "AI 대화 내역 조회", description = "내가 나눈 AI 대화 내역을 최신순으로 조회합니다.")
    @GetMapping("/chat/history")
    public ResponseEntity<List<AiChatHistoryResponse>> getHistory(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(aiChatService.getHistory(token));
    }
}
